package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.MutableTap
import kampfire.model.toDataOr
import kampfire.model.Tap
import kampfire.model.combine
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.io.MixDb
import streetlight.model.music.Clip
import streetlight.model.music.Mix
import streetlight.model.music.MixId
import streetlight.model.music.SectionClipId
import streetlight.model.music.TrackSectionId
import streetlight.model.music.mixOf
import streetlight.model.music.ui.SectionNotation
import streetlight.model.music.ui.sectionNotationsOf

/**
 * Edits the mixes in [mixDb] of the music in [musicEditor], their clips read from [clipLibrary] and their takes from
 * [takeShelf], with problems going to [messenger].
 */
class MixEditor(
    scope: CoroutineScope,
    val musicEditor: MusicEditor,
    private val mixDb: MixDb,
    private val clipLibrary: ClipLibrary,
    private val takeShelf: TakeShelf,
    private val messenger: Messenger,
) {
    private val state = storeOf(MixEditorState())

    /** The mix being edited, or `null` while the mixes load. */
    val mixState: MutableTap<Mix?> = state.mutableTapOf(
        { editor -> editor.mixes.firstOrNull { it.mixId == editor.mixId } },
    ) { mix ->
        if (mix == null) this else copy(mixes = mixes.map { if (it.mixId == mix.mixId) mix else it })
    }

    /** The seconds a tick lasts in playback: at the tempo of the mix being edited, or of the music before one loads. */
    val playbackSecondsPerTickState = mixState.combine(musicEditor.musicState) { mix, music ->
        60.0 / (mix?.bpm ?: music.bpm) / music.time.pulse.ticks
    }

    /** Whether a mix is open to edit. */
    val isOpenState = mixState.tapOf { it != null }

    /** The mixes of the music. */
    val mixesState: Tap<List<Mix>> = state.tapOf { it.mixes }

    /** The mix being edited, which a write opens another of the mixes as. */
    val openMixState: MutableTap<Mix?> = state.mutableTapOf(
        { editor -> editor.mixes.firstOrNull { it.mixId == editor.mixId } },
    ) { mix ->
        if (mix == null) this else copy(mixId = mix.mixId)
    }

    /** A form of a new mix, at the tempo and sounding key of the music, under the first free name. */
    fun newMixForm(): MixForm {
        val names = state.now.mixes.map { it.name }.toSet()
        val name = generateSequence(1) { it + 1 }.map { "$MIX_NAME_BASE $it" }.first { it !in names }
        return MixForm(mixOf(musicEditor.musicState.now, name))
    }

    /**
     * Reads the mix of [form] into the mixes, in place of the one it was seeded from, or as a new mix that opens.
     * Returns whether the form could be read.
     */
    fun applyMixForm(form: MixForm): Boolean {
        val mix = form.readMix().toDataOr(messenger) { return false }
        state.update { editor ->
            if (editor.mixes.any { it.mixId == mix.mixId }) {
                // only what the form holds, so clips bound since it was seeded stay
                editor.copy(mixes = editor.mixes.map {
                    if (it.mixId == mix.mixId) it.copy(name = mix.name, bpm = mix.bpm, soundingKey = mix.soundingKey) else it
                })
            } else editor.copy(mixes = editor.mixes + mix, mixId = mix.mixId)
        }
        return true
    }

    private val clipsStore = storeOf<List<Clip>>(emptyList())

    /** The mix being edited without its clips and hidden sections, so it changes only with its properties. */
    val propertiesState: Tap<Mix?> = mixState.tapOf { it?.copy(clips = emptyList(), hidden = emptyList()) }

    /** Each section of each track the open mix shows, in the order of the tracks. */
    val shownSectionsState: Tap<List<MixSection>> = musicEditor.storedState.combine(mixState) { song, mix ->
        mixSectionsOf(song).filter { it.trackSectionId !in mix?.hidden.orEmpty() }
    }

    /** Each section the open mix hides that a track still holds. */
    val hiddenSectionsState: Tap<List<MixSection>> = musicEditor.storedState.combine(mixState) { song, mix ->
        mixSectionsOf(song).filter { it.trackSectionId in mix?.hidden.orEmpty() }
    }

    /** The section whose takes are being recorded, in place of the overview, or `null`. */
    val focusState: Tap<TrackSectionId?> = state.tapOf { it.focus }

    /** Shows [trackSectionId] alone, to record its takes. */
    fun focusSection(trackSectionId: TrackSectionId) = state.update { it.copy(focus = trackSectionId) }

    /** Returns from the section in focus to the overview. */
    fun clearFocus() = state.update { it.copy(focus = null) }

    /** Leaves [trackSectionId] out of the overview of the open mix. */
    fun hideSection(trackSectionId: TrackSectionId) =
        updateOpenMix { it.copy(hidden = it.hidden - trackSectionId + trackSectionId) }

    /** Returns [trackSectionId] to the overview of the open mix. */
    fun showSection(trackSectionId: TrackSectionId) = updateOpenMix { it.copy(hidden = it.hidden - trackSectionId) }

    private fun mixSectionsOf(song: StoredSong) = song.tracks.flatMap { track ->
        track.sections.map { section ->
            MixSection(TrackSectionId(track.trackId, section.sectionId), "${track.name} · ${song.music.nameOf(section.sectionId)}")
        }
    }

    /** The names of the track and section of [trackSectionId], as `Melody · Verse`. */
    fun labelOf(trackSectionId: TrackSectionId): String {
        val trackName = musicEditor.readTrack(trackSectionId.trackId)?.name.orEmpty()
        return "$trackName · ${musicEditor.musicState.now.nameOf(trackSectionId.sectionId)}"
    }


    init {
        scope.launch {
            loadMixes()
            saveMixChanges()
        }
        scope.launch { musicEditor.trackChanges.collect { applyTrackChange(it) } }
        scope.launch { mixState.flow.collect { clipsStore.set(clipLibrary.readClips()) } }
    }

    /** The takes of the open mix that still fit the song, or `null` while the mixes load. */
    suspend fun readTakes(): SongTakes? {
        val mix = mixState.now ?: return null
        return takeShelf.takesOf(musicEditor.storedState.now, mix)
    }

    /** Binds [clip] to its section in the mix being edited, in place of the clip it held. */
    fun putClip(clip: SectionClipId) = updateOpenMixClips { clips ->
        clips.filterNot { it.trackSectionId == clip.trackSectionId } + clip
    }

    /** Unbinds the clip of [trackSectionId] in the mix being edited. */
    fun removeClip(trackSectionId: TrackSectionId) = updateOpenMixClips { clips ->
        clips.filterNot { it.trackSectionId == trackSectionId }
    }

    /** The name of the clip bound to [trackSectionId], marked when it no longer fits, or `null` while tones play it. */
    fun clipLabelOf(trackSectionId: TrackSectionId): Tap<String?> = clipsStore.combine(mixState) { clips, mix ->
        val clipId = mix?.clips?.firstOrNull { it.trackSectionId == trackSectionId }?.clipId ?: return@combine null
        val clip = clips.firstOrNull { it.clipId == clipId } ?: return@combine null
        val track = musicEditor.readTrack(trackSectionId.trackId) ?: return@combine null
        if (clip.fits(musicEditor.musicState.now, mix, track, trackSectionId.sectionId)) clip.name else "${clip.name} (stale)"
    }

    /** The notation of [trackSectionId], or `null` once its track no longer holds it. */
    fun notationOf(trackSectionId: TrackSectionId): Tap<SectionNotation?> =
        musicEditor.storedState.combine(musicEditor.isLyricsShownState) { song, isLyricsShown ->
            val track = song.tracks.firstOrNull { it.trackId == trackSectionId.trackId } ?: return@combine null
            sectionNotationsOf(track, song.music, song.tracks, isLyricsShown)
                .firstOrNull { it.sectionId == trackSectionId.sectionId }
        }

    private fun updateOpenMixClips(transform: (List<SectionClipId>) -> List<SectionClipId>) =
        updateOpenMix { it.copy(clips = transform(it.clips)) }

    private fun updateOpenMix(transform: (Mix) -> Mix) = state.update { editor ->
        editor.copy(mixes = editor.mixes.map { if (it.mixId == editor.mixId) transform(it) else it })
    }

    private fun applyTrackChange(change: TrackChange) = state.update { editor ->
        val mixes = editor.mixes.map { mix ->
            when (change) {
                is TrackChange.Removed -> mix.copy(
                    clips = mix.clips.filter { it.trackSectionId.trackId != change.trackId },
                    hidden = mix.hidden.filter { it.trackId != change.trackId },
                )
            }
        }
        val focus = when (change) {
            is TrackChange.Removed -> editor.focus?.takeIf { it.trackId != change.trackId }
        }
        editor.copy(mixes = mixes, focus = focus)
    }

    private suspend fun loadMixes() {
        val music = musicEditor.musicState.now
        val stored = mixDb.readMixes(music.musicId).toDataOr(messenger) { return }
        val mixes = stored.ifEmpty { listOf(mixOf(music, "$MIX_NAME_BASE 1").also { save(it) }) }
        state.update { editor -> editor.copy(mixes = mixes, mixId = editor.mixId ?: mixes.first().mixId) }
    }

    /** Saves each mix as it changes, from whatever edit changes it. */
    private suspend fun saveMixChanges() {
        var saved = state.now.mixes
        state.flow.map { it.mixes }.distinctUntilChanged().collect { mixes ->
            mixes.filter { it !in saved }.forEach { save(it) }
            saved = mixes
        }
    }

    private suspend fun save(mix: Mix) {
        mixDb.saveMix(mix).toDataOr(messenger) { return }
    }
}

data class MixEditorState(
    val mixes: List<Mix> = emptyList(),
    val mixId: MixId? = null,
    val focus: TrackSectionId? = null,
)

/** A section of a track as the mix tab shows it, under [label], as `Melody · Verse`. */
data class MixSection(
    val trackSectionId: TrackSectionId,
    val label: String,
)

private const val MIX_NAME_BASE = "Mix"
