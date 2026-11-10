package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.storeOf
import streetlight.model.music.Chroma
import streetlight.model.music.Key
import streetlight.model.music.Mix
import streetlight.model.music.Mode

/** The properties of [seed] as a form holds them, apart from the mix until they are read back onto it. */
class MixForm(private val seed: Mix) {
    val nameState = storeOf(seed.name)

    /** The tempo as text, counting the pulse as the music's does. */
    val bpmTextState = storeOf(seed.bpm.toString())

    /** The tonic of the sounding key, by name. */
    val tonicState = storeOf(TONICS[seed.soundingKey.tonic.value])

    /** The mode of the sounding key, by name. */
    val modeState = storeOf(seed.soundingKey.mode.name)

    /** The seed with the properties of the form, or the problem that keeps them from being read. */
    fun readMix(): Outcome<Mix> {
        val name = nameState.now.trim().ifEmpty { return MixFormProblem.name }
        val bpm = bpmTextState.now.trim().toIntOrNull()?.takeIf { it in BPM_RANGE } ?: return MixFormProblem.tempo
        val tonic = TONICS.indexOf(tonicState.now).takeIf { it >= 0 } ?: seed.soundingKey.tonic.value
        val mode = Mode.entries.firstOrNull { it.name == modeState.now } ?: seed.soundingKey.mode
        return Ok(seed.copy(name = name, bpm = bpm, soundingKey = Key(Chroma.of(tonic), mode)))
    }

    companion object {
        /** The name of each tonic, by its semitones above C. */
        val TONICS = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")
        val MODES = Mode.entries.map { it.name }
    }
}

/** Problems of reading a mix from its form. */
object MixFormProblem {
    val name = Problem("A mix needs a name")
    val tempo = Problem("A tempo is a whole number from ${BPM_RANGE.first} to ${BPM_RANGE.last}")
}

private val BPM_RANGE = 20..400
