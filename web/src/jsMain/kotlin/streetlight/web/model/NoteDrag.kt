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
import streetlight.model.music.Music
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Seventh
import streetlight.model.music.Track
import streetlight.model.music.ui.NoteNotation
import streetlight.model.music.ui.NoteSource
import streetlight.model.music.ui.NoteValue
import streetlight.model.music.ui.StaffWindow
import streetlight.model.music.ui.midiOfStaffStep
import streetlight.model.music.ui.staffStepOf

/** A note of [section] in the lane at [laneIndex] being dragged from [source] to [moved], drawn as [cursor]. */
data class NoteDrag(
    val laneIndex: Int,
    val section: LaneSection,
    val source: NoteSource,
    val moved: NoteSource,
    val cursor: NoteNotation,
)

/**
 * This note with its onset at the grid line of [step] nearest [tick], within its measure and the notes either side in
 * [track]. Its end stays where it is. A pickup keeps its place.
 */
fun NoteSource.snapTick(tick: Double, track: Track, music: Music, step: NoteValue): NoteSource {
    if (isPickup) return this
    val measure = music.time.measure.ticks
    val measureStart = this.tick / measure * measure
    val previous = previousTicks(track)
    val low = max(measureStart, previous?.let { this.tick - it + MIN_NOTE_TICKS } ?: this.tick)
    val high = min(measureStart + measure - 1, this.tick + ticks - MIN_NOTE_TICKS)

    val gridLow = measureStart + ceil((low - measureStart).toDouble() / step.ticks).toInt() * step.ticks
    val gridHigh = measureStart + floor((high - measureStart).toDouble() / step.ticks).toInt() * step.ticks
    if (gridLow > gridHigh) return this
    val nearest = measureStart + ((tick - measureStart) / step.ticks).roundToInt() * step.ticks
    val onset = nearest.coerceIn(gridLow, gridHigh)
    return copy(tick = onset, ticks = ticks - (onset - this.tick))
}

/** The length of the note before this one in [track], or `null` for the first of its section. */
private fun NoteSource.previousTicks(track: Track) = when (track) {
    is MelodyTrack -> track.sections.firstOrNull { it.sectionId == sectionId }?.notes?.getOrNull(index - 1)?.beats?.ticks
    is ChordTrack -> track.sections.firstOrNull { it.sectionId == sectionId }?.notes?.getOrNull(index - 1)?.beats?.ticks
    else -> null
}

/**
 * This note moved [stepsUp] staff steps from [from] within [window]: by the lines of the scale of [key] when
 * [isScaleSnap], and otherwise by semitones. A chord moves its root within one octave, so it never jumps, and follows
 * the key when [isScaleSnap].
 */
fun NoteSource.snapPitch(from: NoteSource, stepsUp: Double, key: Key, window: StaffWindow, isScaleSnap: Boolean): NoteSource {
    val chord = from.chord
    val steps = stepsUp.roundToInt()
    val low = if (chord == null) window.low else staffStepOf(CHORD_ROOT_LOW, key)
    val high = if (chord == null) window.high else staffStepOf(CHORD_ROOT_LOW + 11, key)
    val midi = when {
        !isScaleSnap -> {
            val semitones = from.midi + (stepsUp * SEMITONES_PER_STEP).roundToInt()
            if (chord == null) semitones.coerceIn(midiOfStaffStep(low, key), midiOfStaffStep(high, key))
            else semitones.coerceIn(CHORD_ROOT_LOW, CHORD_ROOT_LOW + 11)
        }
        steps == 0 -> from.midi
        else -> midiOfStaffStep((staffStepOf(from.midi, key) + steps).coerceIn(low, high), key)
    }
    if (chord == null) return copy(midi = midi)
    if (midi == from.midi) return copy(midi = midi, chord = chord)
    return copy(midi = midi, chord = chord.withRoot(midi, key, isScaleSnap))
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
fun Track.withMovedNote(source: NoteSource, moved: NoteSource, key: Key): Track = when (this) {
    is MelodyTrack -> copy(sections = sections.map { phrase ->
        if (phrase.sectionId == source.sectionId) phrase.withMovedNote(source, moved, key) else phrase
    })
    is ChordTrack -> copy(sections = sections.map { progression ->
        if (progression.sectionId == source.sectionId) progression.withMovedNote(source, moved) else progression
    })
    else -> this
}

private fun Phrase.withMovedNote(source: NoteSource, moved: NoteSource, key: Key): Phrase {
    val interval = Interval.ofMidi(moved.midi, key)
    if (source.isPickup) {
        return copy(pickup = pickup.toMutableList().apply { this[source.index] = this[source.index].copy(interval = interval) })
    }
    val notes = notes.withMoved(
        source = source,
        moved = moved,
        withTicks = { note, ticks -> note.copy(beats = Beats(ticks)) },
        ticksOf = { it.beats.ticks },
        restOf = { MelodyNote(null, Beats(it)) },
        withPitch = { it.copy(interval = interval) },
    )
    return copy(notes = notes)
}

private fun Progression.withMovedNote(source: NoteSource, moved: NoteSource): Progression {
    if (source.isPickup) {
        return copy(pickup = pickup.toMutableList().apply { this[source.index] = this[source.index].copy(chord = moved.chord) })
    }
    val notes = notes.withMoved(
        source = source,
        moved = moved,
        withTicks = { note, ticks -> note.copy(beats = Beats(ticks)) },
        ticksOf = { it.beats.ticks },
        restOf = { ChordNote(null, Beats(it)) },
        withPitch = { it.copy(chord = moved.chord) },
    )
    return copy(notes = notes)
}

/** These notes with the one at [source] moved, the note before it taking up the difference. */
private fun <N> List<N>.withMoved(
    source: NoteSource,
    moved: NoteSource,
    withTicks: (N, Int) -> N,
    ticksOf: (N) -> Int,
    restOf: (Int) -> N,
    withPitch: (N) -> N,
): List<N> {
    val shift = moved.tick - source.tick
    val notes = toMutableList()
    notes[source.index] = withTicks(withPitch(notes[source.index]), moved.ticks)
    when {
        source.index > 0 -> notes[source.index - 1] = notes[source.index - 1].let { withTicks(it, ticksOf(it) + shift) }
        // the first note has none before it to lengthen, so a rest fills the room it leaves
        shift > 0 -> notes.add(0, restOf(shift))
    }
    return notes
}

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
