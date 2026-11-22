package streetlight.model.music.ui

import streetlight.model.music.Beats
import streetlight.model.music.Chord
import streetlight.model.music.Key
import streetlight.model.music.NotationId
import kotlin.math.roundToInt


/**
 * The layout of [notationId] in a track, shown as [name], [ticks] long and [steps] of the staff tall, with a step the distance from a
 * line to the space beside it. The bottom line of the staff sits [staffStep] steps up. A section with no [hasStaff] is
 * drawn as its vocals alone. A full line of its text is [lineTicks] long.
 */
data class NotationLayout(
    val notationId: NotationId,
    val name: String,
    val ticks: Int,
    val steps: Int,
    val staffStep: Int,
    val measures: List<MeasureLayout>,
    val hasStaff: Boolean,
    val lineTicks: Int,
) {
    /** The number of lines of vocals sung to the section. */
    val vocalLines get() = measures.first().vocals.size

    /** The length of the pickup, as a share of the section. */
    val pickupShare get() = measures.firstOrNull { it.isPickup }?.width ?: 0.0

    /** The distance the staff sits ahead of the section, a sixteenth, as a share of the section. */
    val staffLeadShare get() = NoteValue.Sixteenth.ticks.toDouble() / ticks
}

/**
 * The layout of each line of this section as its text breaks it, each line's measures sharing the line alone and
 * the pickup leading the first.
 */
fun NotationLayout.lines(): List<NotationLayout> =
    measures.groupBy { it.line }.values.map { lineMeasures ->
        val share = lineMeasures.filterNot { it.isPickup }.sumOf { it.width }
        copy(
            ticks = (ticks * share).roundToInt(),
            measures = lineMeasures.map { it.copy(width = it.width / share) },
        )
    }

/**
 * The measure numbered [number] of its section, on [line] of its text, filling [width] of it, with the [notes] that
 * start in it and the [vocals] of each line of its section that start in it. A pickup is numbered 0 and sounds before
 * the section starts.
 */
data class MeasureLayout(
    val number: Int,
    val line: Int,
    val width: Double,
    val notes: List<NoteLayout>,
    val vocals: List<List<VocalLayout>>,
) {
    val isPickup get() = number == 0
}

/**
 * A syllable of [text] starting [onset] of the way through its measure.
 */
data class VocalLayout(
    val onset: Double,
    val text: String,
)

/**
 * A note or chord starting [onset] of the way through its measure, its lowest head centered [step] steps above the
 * bottom of its section. Each of its [ledgers] is a ledger line, in steps
 * from that lowest head, as its [heads] are placed. A note [isRaised] sits between two degrees of the scale, on the line
 * of the lower. Its [value] is `null` for heads alone, and its [source] is its place in its track, or `null` for the
 * hits of a drum track, which draw as one note.
 */
data class NoteLayout(
    val onset: Double,
    val step: Int,
    val value: NoteValue?,
    val isDotted: Boolean,
    val heads: List<NoteHead>,
    val ledgers: List<Int>,
    val isRaised: Boolean,
    val source: NoteSource?,
)

/** A head of a note, [step] steps above its lowest head. */
data class NoteHead(
    val step: Int,
    val shape: HeadShape,
)

/** The shape of a note's head: round for pitches and drums, a cross for cymbals. */
enum class HeadShape {
    Round,
    Cross,
}

/**
 * The place of a note in its track: [index] of the notes of [notationId], or of its pickup when [isPickup], drawn at
 * [midi] for [ticks] from [tick] of its notation. A chord's [midi] is its root, and [chord] is what it plays.
 */
data class NoteSource(
    val notationId: NotationId,
    val index: Int,
    val isPickup: Boolean,
    val tick: Int,
    val ticks: Int,
    val midi: Int,
    val chord: Chord? = null,
)

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
