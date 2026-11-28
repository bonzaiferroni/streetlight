package streetlight.model.music.ui

import streetlight.model.music.Beats
import streetlight.model.music.Chord
import streetlight.model.music.Key
import streetlight.model.music.LyricTrack
import streetlight.model.music.MusicSheet
import streetlight.model.music.Notation
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.TrackId
import streetlight.model.music.toMidi

/** The shape of a note's head: round for pitches and drums, a cross for cymbals. */
enum class HeadShape {
    Round,
    Cross,
}

/** The written length of a note. */
enum class NoteValue(val ticks: Int) {
    Whole(Beats.TICKS_PER_BEAT * 4),
    Half(Beats.TICKS_PER_BEAT * 2),
    Quarter(Beats.TICKS_PER_BEAT),
    Eighth(Beats.TICKS_PER_BEAT / 2),
    Sixteenth(Beats.TICKS_PER_BEAT / 4),
}

/** The staff steps a track's notation shows, from [low] to [high], with each step counted from C of octave 0. */
data class StaffWindow(
    val low: Int,
    val high: Int,
) {
    /** The steps of the window, with two to spare at each end for the heads and bars of its outermost notes. */
    val steps get() = high - low + 4

    /** The steps [staffStep] sits above the bottom of the window. */
    fun stepOf(staffStep: Int) = staffStep - low + 2

    /** The steps the bottom line of the staff sits above the bottom of the window. */
    val staffStep get() = stepOf(STAFF_LOW)
}

/** The window of [trackId]: the steps its notes reach and the staff, with [dragRoom] steps to spare at each end. */
fun MusicSheet.staffWindowOf(trackId: TrackId, dragRoom: Int = DRAG_ROOM_STEPS): StaffWindow {
    val track = tracks.first { it.trackId == trackId }
    if (track is LyricTrack) return LYRIC_WINDOW
    val steps = track.notations.flatMap { drawnMidisOf(it, music.key) }.map { staffStepOf(it, music.key) }
    val low = minOf(steps.minOrNull() ?: STAFF_LOW, STAFF_LOW)
    val high = maxOf(steps.maxOrNull() ?: STAFF_HIGH, STAFF_HIGH)
    return StaffWindow(low - dragRoom, high + dragRoom)
}

/** The MIDI numbers [chord] is drawn at in [key], an octave above where it sounds, so it sits on the treble staff. */
fun midiChordOf(chord: Chord, key: Key) = chord.toMidi(key).map { it + CHORD_DRAWN_SEMITONES }

/** The MIDI numbers of every note [notation] draws in [key]. */
private fun drawnMidisOf(notation: Notation, key: Key): List<Int> = when (notation) {
    is Phrase -> notation.notes.mapNotNull { it.interval?.toMidi(key) }
    is Progression -> notation.notes.flatMap { note -> note.chord?.let { midiChordOf(it, key) }.orEmpty() }
    else -> emptyList()
}

/**
 * The staff step of [midi] in [key], counted from C of octave 0. A note takes the line or space of its scale degree
 * from the major tonic, and a note between degrees shares the line of the degree below it.
 */
fun staffStepOf(midi: Int, key: Key): Int {
    val tonic = key.majorTonic.value
    val semitones = midi - tonic
    return semitones.floorDiv(12) * 7 + TONIC_STEPS[tonic] + DEGREE_STEPS[semitones.mod(12)]
}

/** The MIDI number of the degree of the scale of [key] on [staffStep], the inverse of [staffStepOf] for notes of the scale. */
fun midiOfStaffStep(staffStep: Int, key: Key): Int {
    val tonic = key.majorTonic.value
    val steps = staffStep - TONIC_STEPS[tonic]
    return tonic + steps.floorDiv(7) * 12 + SCALE_SEMITONES[steps.mod(7)]
}

/** The letter each major tonic is spelled on, counted from C, as flats name them in all but F#. */
private val TONIC_STEPS = listOf(0, 1, 1, 2, 2, 3, 3, 4, 5, 5, 6, 6)

/** The steps above the tonic of each semitone above it, the sharp of a degree sharing its line. */
private val DEGREE_STEPS = listOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6)

/** The semitones above the tonic of each degree of the scale. */
private val SCALE_SEMITONES = listOf(0, 2, 4, 5, 7, 9, 11)

/** The bottom line of the treble staff, E4, and its top line, F5. */
internal const val STAFF_LOW = 37
internal const val STAFF_HIGH = 45

/** The semitones above the tonic that fall between degrees of the scale. */
internal val RAISED_SEMITONES = setOf(1, 3, 6, 8, 10)

/** The steps of room to drag a note past the highest and lowest of a track. */
private const val DRAG_ROOM_STEPS = 2

/** The window of a lyric track, which has no staff: tall enough for a line of text. */
private val LYRIC_WINDOW = StaffWindow(0, 0)

/** The semitones a chord is drawn above where it sounds. */
private const val CHORD_DRAWN_SEMITONES = 12
