package streetlight.model.music.ui

import streetlight.model.music.Beats
import streetlight.model.music.Chord
import streetlight.model.music.Drum
import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.LyricTrack
import streetlight.model.music.Music
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.Stanza
import streetlight.model.music.Track
import streetlight.model.music.lengthOf
import streetlight.model.music.sectionsOf
import streetlight.model.music.toMidi
import kotlin.math.roundToInt


/**
 * The notation of [sectionId] in a track, [ticks] long and [steps] of the staff tall, with a step the distance from a
 * line to the space beside it. The bottom line of the staff sits [staffStep] steps up. The [pickup] notes sound before
 * and [pickupSyllables] the section starts. A section with no [hasStaff] is drawn as its syllables alone. Its text
 * holds [measuresPerLine] measures of [measureTicks] to a line.
 */
data class SectionNotation(
    val sectionId: SectionId,
    val ticks: Int,
    val steps: Int,
    val staffStep: Int,
    val measures: List<MeasureNotation>,
    val pickup: List<NoteNotation>,
    val pickupSyllables: List<SyllableNotation>,
    val hasStaff: Boolean,
    val measuresPerLine: Int,
    val measureTicks: Int,
) {
    /** The length of the pickup, as a share of the section. */
    val pickupShare get() = -((pickup.map { it.onset } + pickupSyllables.map { it.onset }).minOrNull() ?: 0.0)

    /** The distance the staff sits ahead of the section, a sixteenth, as a share of the section. */
    val staffLeadShare get() = NoteValue.Sixteenth.ticks.toDouble() / ticks
}

/**
 * The notation of each line of this section as its text breaks it, each line's measures sharing the line alone and
 * the pickup leading the first.
 */
fun SectionNotation.lines(): List<SectionNotation> =
    measures.chunked(measuresPerLine.coerceAtLeast(1)).mapIndexed { index, lineMeasures ->
        val share = lineMeasures.sumOf { it.share }
        copy(
            ticks = (ticks * share).roundToInt(),
            measures = lineMeasures.map { it.copy(share = it.share / share) },
            pickup = if (index == 0) pickup.map { it.copy(onset = it.onset / share) } else emptyList(),
            pickupSyllables = if (index == 0) pickupSyllables.map { it.copy(onset = it.onset / share) } else emptyList(),
        )
    }

/** A measure filling [share] of its section, with the [notes] and [syllables] that start in it. */
data class MeasureNotation(
    val share: Double,
    val notes: List<NoteNotation>,
    val syllables: List<SyllableNotation>,
)

/** A syllable of [text] starting [onset] of the way through its measure, or for a pickup that far before its section. */
data class SyllableNotation(
    val onset: Double,
    val text: String,
)

/**
 * A note or chord starting [onset] of the way through its measure, or for a pickup that far before its section, its
 * lowest head centered [step] steps above the bottom of its section. Each of its [ledgers] is a ledger line, in steps
 * from that lowest head, as its [heads] are placed. A note [isRaised] sits between two degrees of the scale, on the line
 * of the lower. Its [value] is `null` for heads alone, and its [source] is its place in its track, or `null` for the
 * hits of a drum track, which draw as one note.
 */
data class NoteNotation(
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
    if (track is LyricTrack) return LYRIC_WINDOW
    val steps = track.sections.flatMap { drawnMidisOf(it, key) }.map { staffStepOf(it, key) }
    val low = minOf(steps.minOrNull() ?: STAFF_LOW, STAFF_LOW)
    val high = maxOf(steps.maxOrNull() ?: STAFF_HIGH, STAFF_HIGH)
    return StaffWindow(low - DRAG_ROOM_STEPS, high + DRAG_ROOM_STEPS)
}

/** The notation of [sectionId] in [track] against [music] within [window], or `null` when it has no length there. */
fun sectionNotationOf(track: Track, sectionId: SectionId, music: Music, window: StaffWindow): SectionNotation? {
    val ticks = track.lengthOf(sectionId)?.ticks?.takeIf { it > 0 } ?: return null
    val sections = track.sectionsOf(sectionId).orEmpty()
    val notes = noteSourcesOf(sections, music.key).map { it.tick to noteNotationOf(it, 0.0, window, music.key) } +
        drumNotesOf(sections, window)
    val syllables = syllablesOf(sections)
    val measure = music.time.measure.ticks
    val count = (ticks + measure - 1) / measure
    val measures = List(count) { index ->
        val start = index * measure
        val length = minOf(measure, ticks - start)
        val measureNotes = notes.filter { (tick) -> tick in start until start + length }.map { (tick, note) ->
            note.copy(onset = (tick - start).toDouble() / length)
        }
        val measureSyllables = syllables.filter { (tick) -> tick in start until start + length }.map { (tick, text) ->
            SyllableNotation((tick - start).toDouble() / length, text)
        }
        MeasureNotation(length.toDouble() / ticks, measureNotes, measureSyllables)
    }
    val pickup = notes.filter { (tick) -> tick < 0 }.map { (tick, note) -> note.copy(onset = tick.toDouble() / ticks) }
    val pickupSyllables = syllables.filter { (tick) -> tick < 0 }.map { (tick, text) ->
        SyllableNotation(tick.toDouble() / ticks, text)
    }
    return SectionNotation(
        sectionId = sectionId,
        ticks = ticks,
        steps = window.steps,
        staffStep = STAFF_LOW - window.low + 2,
        measures = measures,
        pickup = pickup,
        pickupSyllables = pickupSyllables,
        hasStaff = track !is LyricTrack,
        measuresPerLine = sections.firstOrNull()?.measuresPerLine ?: DEFAULT_MEASURES_PER_LINE,
        measureTicks = measure,
    )
}

/** The notation of each section [track] defines, against [music], leaving out its arrangements of them. */
fun sectionNotationsOf(track: Track, music: Music): List<SectionNotation> {
    val window = staffWindowOf(track, music.key)
    return track.sections.mapNotNull { sectionNotationOf(track, it.sectionId, music, window) }
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
        heads = staffSteps.map { NoteHead(it - lowest, HeadShape.Round) },
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

/** The tick and text of each syllable of the [Stanza]s of [sections], laid end to end, each pickup before its section. */
private fun syllablesOf(sections: List<Section>): List<Pair<Int, String>> {
    val syllables = mutableListOf<Pair<Int, String>>()
    var onset = 0
    for (section in sections) {
        if (section is Stanza) {
            var tick = onset - section.pickup.sumOf { it.beats.ticks }
            for (note in section.pickup + section.notes) {
                note.text?.let { syllables.add(tick to it) }
                tick += note.beats.ticks
            }
        }
        onset += section.length.ticks
    }
    return syllables
}

/** The tick of each note of the [Groove]s of [sections], laid end to end, paired with its notation within [window]. */
private fun drumNotesOf(sections: List<Section>, window: StaffWindow): List<Pair<Int, NoteNotation>> {
    val notes = mutableListOf<Pair<Int, NoteNotation>>()
    var onset = 0
    for (section in sections) {
        if (section is Groove) notes += grooveNotesOf(section, window).map { (tick, note) -> onset + tick to note }
        onset += section.length.ticks
    }
    return notes
}

/** The tick of each note of [groove], paired with its notation within [window]: the hits of a tick as one note. */
private fun grooveNotesOf(groove: Groove, window: StaffWindow): List<Pair<Int, NoteNotation>> {
    return groove.lanes.flatMap { lane ->
        lane.beats.runningFold(0) { tick, beat -> tick + beat.beats.ticks }
            .zip(lane.beats)
            .filter { (_, beat) -> beat.hit != null }
            .map { (tick, _) -> tick to lane.drum }
    }.groupBy({ (tick) -> tick }, { (_, drum) -> drum }).map { (tick, drums) -> tick to drumNotationOf(drums, window) }
}

/** The notation of [drums] struck together within [window], as heads alone. */
private fun drumNotationOf(drums: List<Drum>, window: StaffWindow): NoteNotation {
    val staffSteps = drums.map { staffStepOf(it) }
    val lowest = staffSteps.min()
    return NoteNotation(
        onset = 0.0,
        step = lowest - window.low + 2,
        value = null,
        isDotted = false,
        heads = drums.map { NoteHead(staffStepOf(it) - lowest, headShapeOf(it)) },
        ledgers = ledgersOf(lowest, staffSteps.max()).map { it - lowest },
        isRaised = false,
        source = null,
    )
}

/** The staff step [drum] is written on, counted from C of octave 0. */
private fun staffStepOf(drum: Drum) = when (drum) {
    Drum.Kick -> 38
    Drum.Snare -> 42
    Drum.Tom -> 44
    Drum.Ride -> 45
    Drum.HiHat -> 46
    Drum.Crash -> 47
}

/** The head [drum] is written with. */
private fun headShapeOf(drum: Drum) = when (drum) {
    Drum.Kick, Drum.Snare, Drum.Tom -> HeadShape.Round
    Drum.HiHat, Drum.Crash, Drum.Ride -> HeadShape.Cross
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

/** The window of a lyric track, which has no staff: tall enough for a line of text. */
private val LYRIC_WINDOW = StaffWindow(0, 0)

/** The measures to a line of a section that gives none. */
private const val DEFAULT_MEASURES_PER_LINE = 4
