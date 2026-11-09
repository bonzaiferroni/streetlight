package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.combine
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.music.Chroma
import streetlight.model.music.Clip
import streetlight.model.music.Key
import streetlight.model.music.Mix
import streetlight.model.music.Mode
import streetlight.model.music.ui.SectionNotation
import streetlight.model.music.ui.sectionNotationsOf

/**
 * Edits the mix [editor] has open: its name, tempo and sounding key, each written back to it, and the clip each
 * section of every track plays, with the section selected to work on. A tempo or tonic that doesn't read leaves the
 * mix as it is. The clips of [clipDb] are read again whenever the mix changes, so a new take joins them.
 */
class MixEditor(
    scope: CoroutineScope,
    private val editor: MusicEditor,
    private val clipDb: ClipDb,
) {
    private val mixState = editor.mixState

    /** Whether a mix is open to edit. */
    val isOpenState = mixState.tapOf { it != null }

    val nameState: MutableTap<String> = mixState.mutableTapOf({ it?.name.orEmpty() }) { name -> this?.copy(name = name) }

    /** The tempo as text, counting the pulse as the music's does. */
    val bpmTextState: MutableTap<String> = mixState.mutableTapOf({ it?.bpm?.toString().orEmpty() }) { text ->
        val bpm = text.trim().toIntOrNull()?.takeIf { it in BPM_RANGE } ?: return@mutableTapOf this
        this?.copy(bpm = bpm)
    }

    /** The tonic of the sounding key, by name. */
    val tonicState: MutableTap<String> = mixState.mutableTapOf(
        { it?.let { mix -> TONICS[mix.soundingKey.tonic.value] }.orEmpty() },
    ) { name ->
        val tonic = TONICS.indexOf(name).takeIf { it >= 0 } ?: return@mutableTapOf this
        this?.copy(soundingKey = Key(Chroma.of(tonic), soundingKey.mode))
    }

    /** The mode of the sounding key, by name. */
    val modeState: MutableTap<String> = mixState.mutableTapOf({ it?.soundingKey?.mode?.name.orEmpty() }) { name ->
        val mode = Mode.entries.firstOrNull { it.name == name } ?: return@mutableTapOf this
        this?.copy(soundingKey = soundingKey.copy(mode = mode))
    }

    private val clipsStore = storeOf<List<Clip>>(emptyList())

    /** Each section of each track, in the order of the tracks. */
    val sectionsState: Tap<List<TrackSection>> = editor.tracksState.tapOf { tracks ->
        tracks.flatMap { track -> track.sections.map { TrackSection(track.trackId, it.sectionId) } }
    }

    /** The section selected to work on, or `null`. */
    val selectionState = storeOf<TrackSection?>(null)

    init {
        scope.launch { editor.mixState.flow.collect { clipsStore.set(clipDb.readClips()) } }
    }

    /** The clip status of [section]: its bound clip's name, marked when it no longer fits, or that tones play it. */
    fun statusOf(section: TrackSection): Tap<String> = clipsStore.combine(editor.mixState) { clips, mix ->
        val clipId = mix?.clips?.firstOrNull { it.trackId == section.trackId && it.sectionId == section.sectionId }
            ?.clipId ?: return@combine TONES
        val clip = clips.firstOrNull { it.clipId == clipId } ?: return@combine TONES
        val track = editor.readTrack(section.trackId) ?: return@combine TONES
        if (clip.fits(editor.musicState.now, mix, track, section.sectionId)) clip.name else "${clip.name} (stale)"
    }

    /** The notation of [section], or `null` once its track no longer holds it. */
    fun notationOf(section: TrackSection): Tap<SectionNotation?> =
        editor.tracksState.combine(editor.musicState) { tracks, music ->
            val track = tracks.firstOrNull { it.trackId == section.trackId } ?: return@combine null
            sectionNotationsOf(track, music).firstOrNull { it.sectionId == section.sectionId }
        }

    companion object {
        /** The name of each tonic, by its semitones above C. */
        val TONICS = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")
        val MODES = Mode.entries.map { it.name }
    }
}

private val BPM_RANGE = 20..400
private const val TONES = "(tones)"
