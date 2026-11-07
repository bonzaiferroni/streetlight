package streetlight.web.model

import streetlight.model.data.Beats
import streetlight.model.data.Chord
import streetlight.model.data.Key
import streetlight.model.data.Music
import streetlight.model.data.Phrase
import streetlight.model.data.Progression
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
 * A note or chord starting [onset] of the way through its measure, or for a pickup that far before its section, its
 * lowest head centered [step] steps above the bottom of its section. Each of its [heads] and [ledgers] is a head or
 * ledger line, in steps from that lowest head. A note [isRaised] sits between two degrees of the scale, on the line of
 * the lower.
 */
data class NoteNotation(
    val onset: Double,
    val step: Int,
    val value: NoteValue,
    val isDotted: Boolean,
    val heads: List<Int>,
    val ledgers: List<Int>,
    val isRaised: Boolean,
    val source: NoteSource,
)

/**
 * The place of a note in its track: [index] of the notes of [sectionId], or of its pickup when [isPickup], drawn at
 * [midi] for [ticks] from [tick] of its section. A chord's [midi] is its root, and [chord] is what it plays.
 */
data class NoteSource(
    val sectionId: SectionId,
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
}

/** The window of [track] in [key]: the treble staff, stretched to reach every note of the track, with room to drag past. */
fun staffWindowOf(track: Track, key: Key): StaffWindow {
    val steps = track.sections.flatMap { drawnMidisOf(it, key) }.map { staffStepOf(it, key) }
    val low = minOf(steps.minOrNull() ?: STAFF_LOW, STAFF_LOW)
    val high = maxOf(steps.maxOrNull() ?: STAFF_HIGH, STAFF_HIGH)
    return StaffWindow(low - DRAG_ROOM_STEPS, high + DRAG_ROOM_STEPS)
}

/** The notation of [sectionId] in [track] against [music] within [window], or `null` when it has no length there. */
fun sectionNotationOf(track: Track, sectionId: SectionId, music: Music, window: StaffWindow): SectionNotation? {
    val ticks = track.lengthOf(sectionId)?.ticks?.takeIf { it > 0 } ?: return null
    val sources = noteSourcesOf(track.sectionsOf(sectionId).orEmpty(), music.key)
    val measure = music.time.measure.ticks
    val count = (ticks + measure - 1) / measure
    val measures = List(count) { index ->
        val start = index * measure
        val length = minOf(measure, ticks - start)
        val notes = sources.filter { it.tick in start until start + length }.map {
            noteNotationOf(it, (it.tick - start).toDouble() / length, window, music.key)
        }
        MeasureNotation(length.toDouble() / ticks, notes)
    }
    val pickup = sources.filter { it.tick < 0 }.map { noteNotationOf(it, it.tick.toDouble() / ticks, window, music.key) }
    return SectionNotation(sectionId, ticks, window.steps, STAFF_LOW - window.low + 2, measures, pickup)
}

/** The notation of each section and arrangement [track] defines, against [music]. */
fun sectionNotationsOf(track: Track, music: Music): List<SectionNotation> {
    val window = staffWindowOf(track, music.key)
    return (track.sections.map { it.sectionId } + track.arrangements.map { it.sectionId })
        .mapNotNull { sectionNotationOf(track, it, music, window) }
}

/** The notation of the note at [source], [onset] across what holds it, within [window]. */
fun noteNotationOf(source: NoteSource, onset: Double, window: StaffWindow, key: Key): NoteNotation {
    val (value, isDotted) = noteValueOf(source.ticks)
    val staffSteps = (source.chord?.let { drawnMidisOf(it, key) } ?: listOf(source.midi)).map { staffStepOf(it, key) }
    val lowest = staffSteps.min()
    val isRaised = (source.midi - key.majorTonic.value).mod(12) in RAISED_SEMITONES
    return NoteNotation(
        onset = onset,
        step = lowest - window.low + 2,
        value = value,
        isDotted = isDotted,
        heads = staffSteps.map { it - lowest },
        ledgers = ledgersOf(lowest, staffSteps.max()).map { it - lowest },
        isRaised = isRaised,
        source = source,
    )
}

/** The MIDI numbers [chord] is drawn at in [key], an octave above where it sounds, so it sits on the treble staff. */
fun drawnMidisOf(chord: Chord, key: Key) = chord.toMidi(key).map { it + CHORD_DRAWN_SEMITONES }

/** The MIDI numbers of every note [section] draws in [key]. */
private fun drawnMidisOf(section: Section, key: Key): List<Int> = when (section) {
    is Phrase -> (section.pickup + section.notes).mapNotNull { it.interval?.toMidi(key) }
    is Progression -> (section.pickup + section.notes).flatMap { note -> note.chord?.let { drawnMidisOf(it, key) }.orEmpty() }
    else -> emptyList()
}

/** The place of each note and chord of [sections], laid end to end, each pickup before its section. */
private fun noteSourcesOf(sections: List<Section>, key: Key): List<NoteSource> {
    val sources = mutableListOf<NoteSource>()
    var onset = 0
    for (section in sections) {
        when (section) {
            is Phrase -> {
                fun place(notes: List<Pair<Int?, Int>>, start: Int, isPickup: Boolean) {
                    var tick = start
                    notes.forEachIndexed { index, (midi, ticks) ->
                        if (midi != null) sources.add(NoteSource(section.sectionId, index, isPickup, tick, ticks, midi))
                        tick += ticks
                    }
                }
                val pickup = section.pickup.map { it.interval?.toMidi(key) to it.beats.ticks }
                place(pickup, onset - pickup.sumOf { it.second }, true)
                place(section.notes.map { it.interval?.toMidi(key) to it.beats.ticks }, onset, false)
            }
            is Progression -> {
                fun place(notes: List<Pair<Chord?, Int>>, start: Int, isPickup: Boolean) {
                    var tick = start
                    notes.forEachIndexed { index, (chord, ticks) ->
                        if (chord != null) {
                            val midi = drawnMidisOf(chord, key).first()
                            sources.add(NoteSource(section.sectionId, index, isPickup, tick, ticks, midi, chord))
                        }
                        tick += ticks
                    }
                }
                val pickup = section.pickup.map { it.chord to it.beats.ticks }
                place(pickup, onset - pickup.sumOf { it.second }, true)
                place(section.notes.map { it.chord to it.beats.ticks }, onset, false)
            }
            else -> Unit
        }
        onset += section.length.ticks
    }
    return sources
}

/** The ledger lines a note reaching from [lowest] to [highest] wants, one on each line between it and the staff. */
private fun ledgersOf(lowest: Int, highest: Int) =
    (STAFF_HIGH + 2..highest step 2).toList() + (STAFF_LOW - 2 downTo lowest step 2).toList()

/** The value written for [ticks], the longest that fits, and whether it wants a dot for the half more. */
private fun noteValueOf(ticks: Int): Pair<NoteValue, Boolean> {
    val value = NoteValue.entries.firstOrNull { it.ticks <= ticks } ?: NoteValue.Sixteenth
    return value to (ticks * 2 >= value.ticks * 3)
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

/** The semitones above the tonic that fall between degrees of the scale. */
private val RAISED_SEMITONES = setOf(1, 3, 6, 8, 10)

/** The steps of room to drag a note past the highest and lowest of a track. */
private const val DRAG_ROOM_STEPS = 2

/** The semitones a chord is drawn above where it sounds. */
const val CHORD_DRAWN_SEMITONES = 12

/** The bottom line of the treble staff, E4, and its top line, F5. */
private const val STAFF_LOW = 37
private const val STAFF_HIGH = 45
