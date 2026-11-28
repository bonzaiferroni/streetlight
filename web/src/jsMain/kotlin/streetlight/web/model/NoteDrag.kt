package streetlight.web.model

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import streetlight.model.music.Beats
import streetlight.model.music.Chord
import streetlight.model.music.ChordNote
import streetlight.model.music.ChordQuality
import streetlight.model.music.ChordTrack
import streetlight.model.music.Chroma
import streetlight.model.music.Interval
import streetlight.model.music.Key
import streetlight.model.music.MelodyNote
import streetlight.model.music.MelodyTrack
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Seventh
import streetlight.model.music.TimeSignature
import streetlight.model.music.Track
import streetlight.model.music.pickupCountOf
import streetlight.model.music.ui.ChordEvent
import streetlight.model.music.ui.NotationEvent
import streetlight.model.music.ui.NoteEvent
import streetlight.model.music.ui.NoteValue
import streetlight.model.music.ui.StaffWindow
import streetlight.model.music.ui.midiChordOf
import streetlight.model.music.ui.midiOfStaffStep
import streetlight.model.music.ui.staffStepOf

/** A note of [section] in the lane at [laneIndex] being dragged from [source] to [moved], which draws as its cursor. */
data class NoteDrag(
    val laneIndex: Int,
    val section: LaneSection,
    val source: NotationEvent,
    val moved: NotationEvent,
)

/**
 * This note with its onset at the grid line of [step] nearest [tick], within its measure of [time] and the notes
 * either side in [track]. Its end stays where it is. A pickup keeps its place.
 */
fun NotationEvent.snapTick(tick: Double, track: Track, time: TimeSignature, step: NoteValue): NotationEvent {
    val noteTicks = track.noteTicksOf(notationId) ?: return this
    if (index < pickupCountOf(noteTicks, track.downbeatOf(notationId))) return this
    val measure = time.measure.ticks
    val measureStart = this.tick.floorDiv(measure) * measure
    val previous = noteTicks.getOrNull(index - 1)
    val low = max(measureStart, previous?.let { this.tick - it + MIN_NOTE_TICKS } ?: this.tick)
    val high = min(measureStart + measure - 1, this.tick + beats.ticks - MIN_NOTE_TICKS)

    val gridLow = measureStart + ceil((low - measureStart).toDouble() / step.ticks).toInt() * step.ticks
    val gridHigh = measureStart + floor((high - measureStart).toDouble() / step.ticks).toInt() * step.ticks
    if (gridLow > gridHigh) return this
    val nearest = measureStart + ((tick - measureStart) / step.ticks).roundToInt() * step.ticks
    val onset = nearest.coerceIn(gridLow, gridHigh)
    val moved = Beats(beats.ticks - (onset - this.tick))
    return when (this) {
        is NoteEvent -> copy(tick = onset, beats = moved)
        is ChordEvent -> copy(tick = onset, beats = moved)
        else -> this
    }
}

/**
 * This note moved [stepsUp] staff steps from [from] within [window]: by the lines of the scale of [key] when
 * [isScaleSnap], and otherwise by semitones. A chord moves its root within one octave, so it never jumps, and follows
 * the key when [isScaleSnap].
 */
fun NotationEvent.snapPitch(from: NotationEvent, stepsUp: Double, key: Key, window: StaffWindow, isScaleSnap: Boolean): NotationEvent =
    when {
        this is NoteEvent && from is NoteEvent -> {
            val midi = from.midi ?: return this
            val midiRange = midiOfStaffStep(window.low, key)..midiOfStaffStep(window.high, key)
            copy(midi = snappedMidiOf(midi, stepsUp, key, isScaleSnap, window.low..window.high, midiRange))
        }
        this is ChordEvent && from is ChordEvent -> {
            val chord = from.chord ?: return this
            val root = midiChordOf(chord, key).first()
            val stepRange = staffStepOf(CHORD_ROOT_LOW, key)..staffStepOf(CHORD_ROOT_LOW + 11, key)
            val midi = snappedMidiOf(root, stepsUp, key, isScaleSnap, stepRange, CHORD_ROOT_LOW..CHORD_ROOT_LOW + 11)
            copy(chord = if (midi == root) chord else chord.withRoot(midi, key, isScaleSnap))
        }
        else -> this
    }

/**
 * [midi] moved [stepsUp] staff steps: by the lines of the scale of [key] within [stepRange] when [isScaleSnap], and
 * otherwise by semitones within [midiRange].
 */
private fun snappedMidiOf(
    midi: Int,
    stepsUp: Double,
    key: Key,
    isScaleSnap: Boolean,
    stepRange: IntRange,
    midiRange: IntRange,
): Int {
    val steps = stepsUp.roundToInt()
    return when {
        !isScaleSnap -> (midi + (stepsUp * SEMITONES_PER_STEP).roundToInt()).coerceIn(midiRange)
        steps == 0 -> midi
        else -> midiOfStaffStep((staffStepOf(midi, key) + steps).coerceIn(stepRange), key)
    }
}

/** This chord rooted on [midi] in [key], its bass moved with it, and its quality following the scale when [isScaleSnap]. */
private fun Chord.withRoot(midi: Int, key: Key, isScaleSnap: Boolean): Chord {
    val shift = midi - key.tonic.value - root.value
    val moved = copy(root = Chroma.of(midi - key.tonic.value), bass = bass?.let { Chroma.of(it.value + shift) })
    if (!isScaleSnap || quality == ChordQuality.Sus2 || quality == ChordQuality.Sus4) return moved
    val degree = SCALE_DEGREES[(midi - key.majorTonic.value).mod(12)] ?: return moved
    return moved.copy(quality = SCALE_QUALITIES[degree], seventh = seventh?.let { SCALE_SEVENTHS[degree] })
}

/** This track with the note of [source] moved to where [moved] places it, the note before it taking up the difference. */
fun Track.withMovedNote(source: NotationEvent, moved: NotationEvent, key: Key): Track = when (this) {
    is MelodyTrack -> copy(notations = notations.map { phrase ->
        if (phrase.notationId == source.notationId && moved is NoteEvent) phrase.withMovedNote(source, moved, key) else phrase
    })
    is ChordTrack -> copy(notations = notations.map { progression ->
        if (progression.notationId == source.notationId && moved is ChordEvent) progression.withMovedNote(source, moved) else progression
    })
    else -> this
}

private fun Phrase.withMovedNote(source: NotationEvent, moved: NoteEvent, key: Key): Phrase {
    val interval = moved.midi?.let { Interval.ofMidi(it, key) }
    val notes = notes.withMoved(
        source = source,
        moved = moved,
        pickupCount = pickupCountOf(notes.map { it.beats.ticks }, downbeat),
        withTicks = { note, ticks -> note.copy(beats = Beats(ticks)) },
        ticksOf = { it.beats.ticks },
        restOf = { MelodyNote(null, Beats(it)) },
        withPitch = { it.copy(interval = interval) },
    )
    return copy(notes = notes)
}

private fun Progression.withMovedNote(source: NotationEvent, moved: ChordEvent): Progression {
    val notes = notes.withMoved(
        source = source,
        moved = moved,
        pickupCount = pickupCountOf(notes.map { it.beats.ticks }, downbeat),
        withTicks = { note, ticks -> note.copy(beats = Beats(ticks)) },
        ticksOf = { it.beats.ticks },
        restOf = { ChordNote(null, Beats(it)) },
        withPitch = { it.copy(chord = moved.chord) },
    )
    return copy(notes = notes)
}

/**
 * These notes with the one at [source] moved, the note before it taking up the difference. A note of the first
 * [pickupCount], the pickup, changes its pitch alone, and the pickup never reaches past the downbeat.
 */
private fun <N> List<N>.withMoved(
    source: NotationEvent,
    moved: NotationEvent,
    pickupCount: Int,
    withTicks: (N, Int) -> N,
    ticksOf: (N) -> Int,
    restOf: (Int) -> N,
    withPitch: (N) -> N,
): List<N> {
    val notes = toMutableList()
    if (source.index < pickupCount) {
        notes[source.index] = withPitch(notes[source.index])
        return notes
    }
    val shift = moved.tick - source.tick
    notes[source.index] = withTicks(withPitch(notes[source.index]), moved.beats.ticks)
    when {
        source.index > pickupCount -> notes[source.index - 1] = notes[source.index - 1].let { withTicks(it, ticksOf(it) + shift) }
        // the first note after the downbeat has none of its own before it to lengthen, so a rest fills the room it leaves
        shift > 0 -> notes.add(source.index, restOf(shift))
    }
    return notes
}

/** The ticks of each note of [notationId] in this track, or `null` for a track whose notes are not dragged. */
private fun Track.noteTicksOf(notationId: NotationId) = when (this) {
    is MelodyTrack -> notations.firstOrNull { it.notationId == notationId }?.notes?.map { it.beats.ticks }
    is ChordTrack -> notations.firstOrNull { it.notationId == notationId }?.notes?.map { it.beats.ticks }
    else -> null
}

/** The downbeat of [notationId] in this track. */
private fun Track.downbeatOf(notationId: NotationId) = notations.firstOrNull { it.notationId == notationId }?.downbeat ?: Beats(0)

private const val SEMITONES_PER_STEP = 12.0 / 7
private val MIN_NOTE_TICKS = NoteValue.Sixteenth.ticks

/** The lowest root a chord is drawn on, C4, which makes the octave a chord stays in, so a drag never jumps. */
private const val CHORD_ROOT_LOW = 60

/** The degree of the scale, from 0, of each semitone above its tonic, or `null` between degrees. */
private val SCALE_DEGREES = listOf(0, null, 1, null, 2, 3, null, 4, null, 5, null, 6)

/** The quality the scale gives the triad on each degree. */
private val SCALE_QUALITIES = listOf(
    ChordQuality.Major, ChordQuality.Minor, ChordQuality.Minor, ChordQuality.Major,
    ChordQuality.Major, ChordQuality.Minor, ChordQuality.Diminished,
)

/** The seventh the scale gives the chord on each degree. */
private val SCALE_SEVENTHS = listOf(
    Seventh.Major, Seventh.Minor, Seventh.Minor, Seventh.Major, Seventh.Minor, Seventh.Minor, Seventh.Diminished,
)
