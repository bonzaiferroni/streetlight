package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.storeOf
import streetlight.model.music.Chroma
import streetlight.model.music.Key
import streetlight.model.music.Mode
import streetlight.model.music.Music
import streetlight.model.music.TimeSignature

/** The key, time, tempo and capo of [seed] as a form holds them, apart from the music until they are read onto it. */
class MusicForm(seed: Music) {
    /** The tonic of the key, by name. */
    val tonicState = storeOf(TONIC_NAMES[seed.key.tonic.value])

    /** The mode of the key, by name. */
    val modeState = storeOf(seed.key.mode.name)

    /** The time signature as text, as `3/4`. */
    val timeTextState = storeOf("${seed.time.beats}/${seed.time.beatUnit}")

    /** The tempo as text. */
    val bpmTextState = storeOf(seed.bpm.toString())

    /** The capo as text, blank for none. */
    val capoTextState = storeOf(seed.capo?.toString().orEmpty())

    /** [music] with the properties of the form, or the problem that keeps them from being read. */
    fun readOnto(music: Music): Outcome<Music> {
        val tonic = TONIC_NAMES.indexOf(tonicState.now).takeIf { it >= 0 } ?: music.key.tonic.value
        val mode = Mode.entries.firstOrNull { it.name == modeState.now } ?: music.key.mode
        val time = readTime(timeTextState.now) ?: return MusicFormProblem.time
        val bpm = bpmTextState.now.trim().toIntOrNull()?.takeIf { it in BPM_RANGE } ?: return MusicFormProblem.tempo
        val capo = readCapoText(capoTextState.now) ?: return MusicFormProblem.capo
        return Ok(music.copy(key = Key(Chroma.of(tonic), mode), time = time, bpm = bpm, capo = capo.value))
    }

    // a count of beats over a beat unit that is a power of two
    private fun readTime(text: String): TimeSignature? {
        val beats = text.substringBefore('/', "").trim().toIntOrNull()?.takeIf { it > 0 } ?: return null
        val beatUnit = text.substringAfter('/').trim().toIntOrNull()?.takeIf { it > 0 && it and (it - 1) == 0 } ?: return null
        return TimeSignature(beats, beatUnit)
    }
}

/** Problems of reading a music or a track from its form. */
object MusicFormProblem {
    val time = Problem("A time is a count of beats over a beat unit, as 3/4")
    val tempo = Problem("A tempo is a whole number from ${BPM_RANGE.first} to ${BPM_RANGE.last}")
    val capo = Problem("A capo is a whole number from -$MAX_CAPO to $MAX_CAPO, or blank for none")
}

/** A capo read from a form, where a blank field reads as none. */
class CapoRead(val value: Int?)

/** Reads a capo from [text], blank for none, or returns `null` when it cannot be read. */
fun readCapoText(text: String): CapoRead? {
    val trimmed = text.trim().ifEmpty { return CapoRead(null) }
    return trimmed.toIntOrNull()?.takeIf { it in -MAX_CAPO..MAX_CAPO }?.let { CapoRead(it) }
}

/** The name of each tonic, by its semitones above C. */
val TONIC_NAMES = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")

/** The name of each mode. */
val MODE_NAMES = Mode.entries.map { it.name }

internal val BPM_RANGE = 20..400
private const val MAX_CAPO = 12
