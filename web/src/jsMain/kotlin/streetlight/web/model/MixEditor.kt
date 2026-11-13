@file:OptIn(ExperimentalCoroutinesApi::class)

package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.combine
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapIn
import kampfire.model.tapOf
import kampfire.model.toDataOr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import streetlight.model.music.Clip
import streetlight.model.music.ClipId
import streetlight.model.music.Mix
import streetlight.model.music.MixId
import streetlight.model.music.MusicSheet
import streetlight.model.music.SectionClip
import streetlight.model.music.TrackSectionId
import streetlight.model.music.mixOf
import streetlight.model.music.ui.SectionNotation
import streetlight.model.music.ui.sectionNotationsOf
import streetlight.model.studio.ClipDao
import streetlight.model.studio.MixDao
import streetlight.model.studio.SectionClipDao

/**
 * Edits the mixes in [mixDao] of the music in [musicEditor], and the clips of [clipDao] that [sectionClipDao] binds to
 * their sections, with takes read from [takeShelf] and problems going to [messenger].
 */
class MixEditor(
    private val scope: CoroutineScope,
    val musicEditor: MusicEditor,
    private val mixDao: MixDao,
    private val sectionClipDao: SectionClipDao,
    clipDao: ClipDao,
    private val takeShelf: TakeShelf,
    private val messenger: Messenger,
) {
    private val musicId = musicEditor.musicState.now.musicId
    private val state = storeOf(MixEditorState())
    private val mixMutex = Mutex()

    /** The mixes of the music. */
    val mixesState: Tap<List<Mix>> = state.tapOf { it.mixes }

    /** The mix being edited, or `null` while the mixes load. */
    val mixState: Tap<Mix?> = state.tapOf { it.openMix }

    /** The mix being edited, which a write opens another of the mixes as. */
    val openMixState: MutableTap<Mix?> = state.mutableTapOf({ it.openMix }) { mix -> copy(mixId = mix?.mixId ?: mixId) }

    /** Whether a mix is open to edit. */
    val isOpenState = mixState.tapOf { it != null }

    /** The seconds a tick lasts in playback: at the tempo of the mix being edited, or of the music before one loads. */
    val playbackSecondsPerTickState = mixState.combine(musicEditor.musicState) { mix, music ->
        60.0 / (mix?.bpm ?: music.bpm) / music.time.pulse.ticks
    }

    /** The mix being edited without its hidden sections, so it changes only with its properties. */
    val propertiesState: Tap<Mix?> = mixState.tapOf { it?.copy(hidden = emptyList()) }

    /** The clips of the user's library. */
    val clipsState: Tap<List<Clip>> = clipDao.flowClips().tapIn(scope, emptyList())

    /** The clips bound to sections of the mix being edited. */
    val sectionClipsState: Tap<List<SectionClip>> = mixState.flow
        .map { it?.mixId }
        .distinctUntilChanged()
        .flatMapLatest { mixId -> mixId?.let { sectionClipDao.flowSectionClips(it) } ?: flowOf(emptyList()) }
        .tapIn(scope, emptyList())

    /** Each section of each track the open mix shows, in the order of the tracks. */
    val shownSectionsState: Tap<List<MixSection>> = musicEditor.sheetState.combine(mixState) { sheet, mix ->
        mixSectionsOf(sheet).filter { it.trackSectionId !in mix?.hidden.orEmpty() }
    }

    /** Each section the open mix hides that a track still holds. */
    val hiddenSectionsState: Tap<List<MixSection>> = musicEditor.sheetState.combine(mixState) { sheet, mix ->
        mixSectionsOf(sheet).filter { it.trackSectionId in mix?.hidden.orEmpty() }
    }

    /** The section whose takes are being recorded, in place of the overview, or `null`, as long as its track lasts. */
    val focusState: Tap<TrackSectionId?> = state.combine(musicEditor.tracksState) { editor, tracks ->
        editor.focus?.takeIf { focus -> tracks.any { it.trackId == focus.trackId } }
    }

    init {
        scope.launch { mixDao.flowMixes(musicId).collect { mixes -> state.update { it.copy(mixes = mixes) } } }
        scope.launch {
            if (mixDao.flowMixes(musicId).first().isNotEmpty()) return@launch
            mixDao.saveMix(mixOf(musicEditor.musicState.now, "$MIX_NAME_BASE 1"))
        }
    }

    /** A form of a new mix, at the tempo and sounding key of the music, under the first free name. */
    fun newMixForm(): MixForm {
        val names = mixesState.now.map { it.name }.toSet()
        val name = generateSequence(1) { it + 1 }.map { "$MIX_NAME_BASE $it" }.first { it !in names }
        return MixForm(mixOf(musicEditor.musicState.now, name))
    }

    /**
     * Saves the mix of [form] in place of the one it was seeded from, or as a new mix that opens. Returns whether the
     * form could be read.
     */
    fun applyMixForm(form: MixForm): Boolean {
        val mix = form.readMix().toDataOr(messenger) { return false }
        if (mixesState.now.none { it.mixId == mix.mixId }) {
            scope.launch { mixDao.saveMix(mix) }
            state.update { it.copy(mixId = mix.mixId) }
            return true
        }

        // only what the form holds, so sections hidden since it was seeded stay hidden
        updateMix(mix.mixId) { it.copy(name = mix.name, bpm = mix.bpm, soundingKey = mix.soundingKey) }
        return true
    }

    /** Shows [trackSectionId] alone, to record its takes. */
    fun focusSection(trackSectionId: TrackSectionId) = state.update { it.copy(focus = trackSectionId) }

    /** Returns from the section in focus to the overview. */
    fun clearFocus() = state.update { it.copy(focus = null) }

    /** Leaves [trackSectionId] out of the overview of the open mix. */
    fun hideSection(trackSectionId: TrackSectionId) =
        updateOpenMix { it.copy(hidden = it.hidden - trackSectionId + trackSectionId) }

    /** Returns [trackSectionId] to the overview of the open mix. */
    fun showSection(trackSectionId: TrackSectionId) = updateOpenMix { it.copy(hidden = it.hidden - trackSectionId) }

    /** The names of the track and section of [trackSectionId], as `Melody · Verse`. */
    fun labelOf(trackSectionId: TrackSectionId): String {
        val trackName = musicEditor.readTrack(trackSectionId.trackId)?.name.orEmpty()
        return "$trackName · ${musicEditor.musicState.now.nameOf(trackSectionId.sectionId)}"
    }

    /** The takes of the open mix that still fit the music, or `null` while the mixes load. */
    suspend fun readTakes(): SongTakes? {
        val mix = mixState.now ?: return null
        return takeShelf.takesOf(musicEditor.sheetState.now, mix, sectionClipsState.now)
    }

    /** Binds the clip [clipId] to [trackSectionId] in the mix being edited, in place of the clip it held. */
    fun putClip(trackSectionId: TrackSectionId, clipId: ClipId) {
        val mixId = mixState.now?.mixId ?: return
        scope.launch { sectionClipDao.saveSectionClip(SectionClip(mixId, trackSectionId, clipId)) }
    }

    /** Unbinds the clip of [trackSectionId] in the mix being edited. */
    fun removeClip(trackSectionId: TrackSectionId) {
        val mixId = mixState.now?.mixId ?: return
        scope.launch { sectionClipDao.removeSectionClip(mixId, trackSectionId) }
    }

    /** The id of the clip bound to [trackSectionId] in the mix being edited, or `null` while tones play it. */
    fun boundClipIdOf(trackSectionId: TrackSectionId): Tap<ClipId?> = sectionClipsState.tapOf { sectionClips ->
        sectionClips.firstOrNull { it.trackSectionId == trackSectionId }?.clipId
    }

    /** The name of the clip bound to [trackSectionId], marked when it no longer fits, or `null` while tones play it. */
    fun clipLabelOf(trackSectionId: TrackSectionId): Tap<String?> =
        clipsState.combine(boundClipIdOf(trackSectionId)) { clips, clipId -> clips.firstOrNull { it.clipId == clipId } }
            .combine(mixState) { clip, mix ->
                clip ?: return@combine null
                mix ?: return@combine null
                val track = musicEditor.readTrack(trackSectionId.trackId) ?: return@combine null
                if (clip.fits(musicEditor.musicState.now, mix, track, trackSectionId.sectionId)) clip.name
                else "${clip.name} (stale)"
            }

    /** The notation of [trackSectionId], or `null` once its track no longer holds it. */
    fun notationOf(trackSectionId: TrackSectionId): Tap<SectionNotation?> =
        musicEditor.sheetState.combine(musicEditor.isLyricsShownState) { sheet, isLyricsShown ->
            val track = sheet.tracks.firstOrNull { it.trackId == trackSectionId.trackId } ?: return@combine null
            sectionNotationsOf(track, sheet.music, sheet.tracks, isLyricsShown)
                .firstOrNull { it.sectionId == trackSectionId.sectionId }
        }

    private fun updateOpenMix(transform: (Mix) -> Mix) {
        val mixId = mixState.now?.mixId ?: return
        updateMix(mixId, transform)
    }

    // each change reads the mix as the last one saved it, so quick changes in a row all land
    private fun updateMix(mixId: MixId, transform: (Mix) -> Mix) {
        scope.launch {
            mixMutex.withLock {
                val mix = mixDao.flowMix(mixId).first() ?: return@withLock
                mixDao.saveMix(transform(mix))
            }
        }
    }

    private fun mixSectionsOf(sheet: MusicSheet) = sheet.tracks.flatMap { track ->
        track.sections.map { section ->
            MixSection(TrackSectionId(track.trackId, section.sectionId), "${track.name} · ${sheet.music.nameOf(section.sectionId)}")
        }
    }
}

data class MixEditorState(
    val mixes: List<Mix> = emptyList(),
    val mixId: MixId? = null,
    val focus: TrackSectionId? = null,
) {
    /** The mix opened, or the first of the mixes. */
    val openMix get() = mixes.firstOrNull { it.mixId == mixId } ?: mixes.firstOrNull()
}

/** A section of a track as the mix tab shows it, under [label], as `Melody · Verse`. */
data class MixSection(
    val trackSectionId: TrackSectionId,
    val label: String,
)

private const val MIX_NAME_BASE = "Mix"
