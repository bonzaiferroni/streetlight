package streetlight.web.model

import streetlight.model.data.Beats
import streetlight.model.data.Key
import streetlight.model.data.MelodyNote
import streetlight.model.data.Music
import streetlight.model.data.Phrase
import streetlight.model.data.Section
import streetlight.model.data.SectionId
import streetlight.model.data.Track
import streetlight.model.data.lengthOf
import streetlight.model.data.sectionsOf

/**
 * The notation of [sectionId] in a track, [ticks] long and [steps] of the staff tall, with a step the distance from a
 * line to the space beside it. The bottom line of the staff sits [staffStep] steps up. The [pickup] notes sound before
 * the section starts.
 */
data class SectionNotation(
    val sectionId: SectionId,
    val ticks: Int,
    val steps: Int,
    val staffStep: Int,
    val measures: List<MeasureNotation>,
    val pickup: List<NoteNotation>,
) {
    /** The length of the pickup, as a share of the section. */
    val pickupShare get() = -(pickup.minOfOrNull { it.onset } ?: 0.0)
}

/** A measure filling [share] of its section, with the [notes] that start in it. */
data class MeasureNotation(
    val share: Double,
    val notes: List<NoteNotation>,
)

/**
 * A note starting [onset] of the way through its measure, or for a pickup that far before its section, [step] steps
 * above the bottom of its section. Each of its
 * [ledgers] is a ledger line, in steps from the note's center.
 */
data class NoteNotation(
    val onset: Double,
    val step: Int,
    val value: NoteValue,
    val isDotted: Boolean,
    val ledgers: List<Int>,
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
}

/** The window of [track] in [key]: the treble staff, stretched to reach every note of the track. */
fun staffWindowOf(track: Track, key: Key): StaffWindow {
    val steps = track.sections.filterIsInstance<Phrase>().flatMap { it.pickup + it.notes }
        .mapNotNull { note -> note.interval?.let { staffStepOf(it.toMidi(key), key) } }
    return StaffWindow(minOf(steps.minOrNull() ?: STAFF_LOW, STAFF_LOW), maxOf(steps.maxOrNull() ?: STAFF_HIGH, STAFF_HIGH))
}

/** The notation of [sectionId] in [track] against [music] within [window], or `null` when it has no length there. */
fun sectionNotationOf(track: Track, sectionId: SectionId, music: Music, window: StaffWindow): SectionNotation? {
    val ticks = track.lengthOf(sectionId)?.ticks?.takeIf { it > 0 } ?: return null
    val placed = placedNotesOf(track.sectionsOf(sectionId).orEmpty())
    fun notationOf(note: MelodyNote, onset: Double): NoteNotation? {
        val interval = note.interval ?: return null
        val (value, isDotted) = noteValueOf(note.beats.ticks)
        val staffStep = staffStepOf(interval.toMidi(music.key), music.key)
        return NoteNotation(onset, staffStep - window.low + 1, value, isDotted, ledgersOf(staffStep))
    }
    val measure = music.time.measure.ticks
    val count = (ticks + measure - 1) / measure
    val measures = List(count) { index ->
        val start = index * measure
        val length = minOf(measure, ticks - start)
        val notes = placed.filter { it.tick in start until start + length }.mapNotNull { (tick, note) ->
            notationOf(note, (tick - start).toDouble() / length)
        }
        MeasureNotation(length.toDouble() / ticks, notes)
    }
    val pickup = placed.filter { it.tick < 0 }.mapNotNull { (tick, note) -> notationOf(note, tick.toDouble() / ticks) }
    return SectionNotation(sectionId, ticks, window.steps, STAFF_LOW - window.low + 2, measures, pickup)
}

/** The notation of each section and arrangement [track] defines, against [music]. */
fun sectionNotationsOf(track: Track, music: Music): List<SectionNotation> {
    val window = staffWindowOf(track, music.key)
    return (track.sections.map { it.sectionId } + track.arrangements.map { it.sectionId })
        .mapNotNull { sectionNotationOf(track, it, music, window) }
}

/** A [note] placed at [tick] of its section, before 0 for a pickup. */
private data class PlacedNote(val tick: Int, val note: MelodyNote)

/** The notes of each phrase of [sections], laid end to end with the sections around them, each pickup before its phrase. */
private fun placedNotesOf(sections: List<Section>): List<PlacedNote> {
    val placed = mutableListOf<PlacedNote>()
    var onset = 0
    for (section in sections) {
        if (section is Phrase) {
            var tick = onset - section.pickup.sumOf { it.beats.ticks }
            (section.pickup + section.notes).forEach { note ->
                placed.add(PlacedNote(tick, note))
                tick += note.beats.ticks
            }
        }
        onset += section.length.ticks
    }
    return placed
}

/** The ledger lines a note on [staffStep] wants, one on each line between it and the staff, in steps from the note. */
private fun ledgersOf(staffStep: Int) = when {
    staffStep > STAFF_HIGH -> (STAFF_HIGH + 2..staffStep step 2).toList()
    staffStep < STAFF_LOW -> (STAFF_LOW - 2 downTo staffStep step 2).toList()
    else -> emptyList()
}.map { it - staffStep }

/** The value written for [ticks], the longest that fits, and whether it wants a dot for the half more. */
private fun noteValueOf(ticks: Int): Pair<NoteValue, Boolean> {
    val value = NoteValue.entries.firstOrNull { it.ticks <= ticks } ?: NoteValue.Sixteenth
    return value to (ticks * 2 >= value.ticks * 3)
}

/**
 * The staff step of [midi] in [key], counted from C of octave 0. A note takes the line or space of its scale degree
 * from the major tonic, and a note between degrees shares the line of the degree below it.
 */
private fun staffStepOf(midi: Int, key: Key): Int {
    val tonic = key.majorTonic.value
    val semitones = midi - tonic
    return semitones.floorDiv(12) * 7 + TONIC_STEPS[tonic] + DEGREE_STEPS[semitones.mod(12)]
}

/** The letter each major tonic is spelled on, counted from C, as flats name them in all but F#. */
private val TONIC_STEPS = listOf(0, 1, 1, 2, 2, 3, 3, 4, 5, 5, 6, 6)

/** The steps above the tonic of each semitone above it, the sharp of a degree sharing its line. */
private val DEGREE_STEPS = listOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6)

/** The bottom line of the treble staff, E4, and its top line, F5. */
private const val STAFF_LOW = 37
private const val STAFF_HIGH = 45
