package streetlight.web.model

import streetlight.model.data.Beats
import streetlight.model.data.Interval
import streetlight.model.data.Key
import streetlight.model.data.MelodyNote
import streetlight.model.data.MelodyTrack
import streetlight.model.data.Music
import streetlight.model.data.Phrase
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** A note of [slot] in the lane at [laneIndex] being dragged from [source] to [moved], drawn as [cursor]. */
data class NoteDrag(
    val laneIndex: Int,
    val slot: LaneSlot,
    val source: NoteSource,
    val moved: NoteSource,
    val cursor: NoteNotation,
)

/**
 * This note with its onset at the grid line of [step] nearest [tick], within its measure and the notes either side. Its
 * end stays where it is. A pickup keeps its place.
 */
fun NoteSource.snapTick(tick: Double, phrase: Phrase, music: Music, step: NoteValue): NoteSource {
    if (isPickup) return this
    val measure = music.time.measure.ticks
    val measureStart = this.tick / measure * measure
    val previous = phrase.notes.getOrNull(index - 1)?.beats?.ticks
    val low = max(measureStart, previous?.let { this.tick - it + MIN_NOTE_TICKS } ?: this.tick)
    val high = min(measureStart + measure - 1, this.tick + ticks - MIN_NOTE_TICKS)

    val gridLow = measureStart + ceil((low - measureStart).toDouble() / step.ticks).toInt() * step.ticks
    val gridHigh = measureStart + floor((high - measureStart).toDouble() / step.ticks).toInt() * step.ticks
    if (gridLow > gridHigh) return this
    val nearest = measureStart + ((tick - measureStart) / step.ticks).roundToInt() * step.ticks
    val onset = nearest.coerceIn(gridLow, gridHigh)
    return copy(tick = onset, ticks = ticks - (onset - this.tick))
}

/**
 * This note moved [stepsUp] staff steps from [from] within [window]: by the lines of the scale of [key] when
 * [isScaleSnap], and otherwise by semitones.
 */
fun NoteSource.snapMidi(from: NoteSource, stepsUp: Double, key: Key, window: StaffWindow, isScaleSnap: Boolean): NoteSource {
    val staffStep = staffStepOf(from.midi, key)
    val midi = when {
        !isScaleSnap -> from.midi + (stepsUp * SEMITONES_PER_STEP).roundToInt()
        stepsUp.roundToInt() == 0 -> from.midi
        else -> midiOfStaffStep(staffStep + stepsUp.roundToInt(), key)
    }
    return copy(midi = midi.coerceIn(midiOfStaffStep(window.low, key), midiOfStaffStep(window.high, key)))
}

/** This track with the note of [source] moved to where [moved] places it, the note before it taking up the difference. */
fun MelodyTrack.withMovedNote(source: NoteSource, moved: NoteSource, key: Key) = copy(
    sections = sections.map { phrase ->
        if (phrase.sectionId != source.phraseId) phrase else phrase.withMovedNote(source, moved, key)
    },
)

private fun Phrase.withMovedNote(source: NoteSource, moved: NoteSource, key: Key): Phrase {
    val interval = Interval.ofMidi(moved.midi, key)
    if (source.isPickup) {
        return copy(pickup = pickup.toMutableList().apply { this[source.index] = this[source.index].copy(interval = interval) })
    }
    val shift = moved.tick - source.tick
    val moves = notes.toMutableList()
    moves[source.index] = moves[source.index].copy(interval = interval, beats = Beats(moved.ticks))
    when {
        source.index > 0 -> moves[source.index - 1].let { moves[source.index - 1] = it.copy(beats = Beats(it.beats.ticks + shift)) }
        // the first note has none before it to lengthen, so a rest fills the room it leaves
        shift > 0 -> moves.add(0, MelodyNote(null, Beats(shift)))
    }
    return copy(notes = moves)
}

private const val SEMITONES_PER_STEP = 12.0 / 7
private val MIN_NOTE_TICKS = NoteValue.Sixteenth.ticks
