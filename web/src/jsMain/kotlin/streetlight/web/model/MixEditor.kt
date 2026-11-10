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
import streetlight.model.music.SectionClip
import streetlight.model.music.SectionId
import streetlight.model.music.TrackId
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

    /** Each section of each track, in the order of the tracks. */
    val sectionsState: Tap<List<TrackSection>> = musicEditor.tracksState.tapOf { tracks ->
        tracks.flatMap { track -> track.sections.map { TrackSection(track.trackId, it.sectionId) } }
    }

    /** The section selected to work on, or `null`. */
    val selectionState = storeOf<TrackSection?>(null)

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
    fun putClip(clip: SectionClip) = updateOpenMixClips { clips ->
        clips.filterNot { it.trackId == clip.trackId && it.sectionId == clip.sectionId } + clip
    }

    /** Unbinds the clip of the section [sectionId] of the track [trackId] in the mix being edited. */
    fun removeClip(trackId: TrackId, sectionId: SectionId) = updateOpenMixClips { clips ->
        clips.filterNot { it.trackId == trackId && it.sectionId == sectionId }
    }

    /** The clip status of [section]: its bound clip's name, marked when it no longer fits, or that tones play it. */
    fun statusOf(section: TrackSection): Tap<String> = clipsStore.combine(mixState) { clips, mix ->
        val clipId = mix?.clips?.firstOrNull { it.trackId == section.trackId && it.sectionId == section.sectionId }
            ?.clipId ?: return@combine TONES
        val clip = clips.firstOrNull { it.clipId == clipId } ?: return@combine TONES
        val track = musicEditor.readTrack(section.trackId) ?: return@combine TONES
        if (clip.fits(musicEditor.musicState.now, mix, track, section.sectionId)) clip.name else "${clip.name} (stale)"
    }

    /** The notation of [section], or `null` once its track no longer holds it. */
    fun notationOf(section: TrackSection): Tap<SectionNotation?> =
        musicEditor.storedState.combine(musicEditor.isLyricsShownState) { song, isLyricsShown ->
            val track = song.tracks.firstOrNull { it.trackId == section.trackId } ?: return@combine null
            sectionNotationsOf(track, song.music, song.tracks, isLyricsShown)
                .firstOrNull { it.sectionId == section.sectionId }
        }

    private fun updateOpenMixClips(transform: (List<SectionClip>) -> List<SectionClip>) {
        state.update { editor ->
            editor.copy(mixes = editor.mixes.map { mix ->
                if (mix.mixId != editor.mixId) mix else mix.copy(clips = transform(mix.clips))
            })
        }
    }

    private fun applyTrackChange(change: TrackChange) = state.update { editor ->
        editor.copy(mixes = editor.mixes.map { mix ->
            val clips = when (change) {
                is TrackChange.Renamed -> mix.clips.map { if (it.trackId == change.from) it.copy(trackId = change.to) else it }
                is TrackChange.Removed -> mix.clips.filter { it.trackId != change.trackId }
            }
            mix.copy(clips = clips)
        })
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
)

private const val MIX_NAME_BASE = "Mix"
private const val TONES = "(tones)"
