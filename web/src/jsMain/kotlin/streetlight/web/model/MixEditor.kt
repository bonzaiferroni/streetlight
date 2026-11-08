package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import kampfire.model.tapOf
import streetlight.model.music.Chroma
import streetlight.model.music.Key
import streetlight.model.music.Mix
import streetlight.model.music.Mode

/**
 * The name, tempo and sounding key of the mix at [mixState], each written back to it. A tempo or tonic that doesn't
 * read leaves the mix as it is.
 */
class MixEditor(
    mixState: MutableTap<Mix?>,
) {
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

    companion object {
        /** The name of each tonic, by its semitones above C. */
        val TONICS = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")
        val MODES = Mode.entries.map { it.name }
    }
}

private val BPM_RANGE = 20..400
