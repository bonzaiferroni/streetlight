package streetlight.model.music.ui

import streetlight.model.music.Drum
import streetlight.model.music.Key


/** The events of measure [index] of this track. */
fun TrackSource.eventsOf(index: Int) = events.subList(measures[index].eventStart, measures[index].eventEnd)

/** How far [event] falls across measure [index], from 0 at its start to 1 at its end. */
fun MusicSource.onsetOf(event: NotationEvent, index: Int) =
    (event.tick - measureTickOf(index)).toDouble() / measureBeatsOf(index).ticks

/** The value written for [ticks], the longest that fits. */
fun noteValueOf(ticks: Int) = NoteValue.entries.firstOrNull { it.ticks <= ticks } ?: NoteValue.Sixteenth

/** Whether [ticks] wants a dot on its value for the half more. */
fun isDottedOf(ticks: Int) = ticks * 2 >= noteValueOf(ticks).ticks * 3

/** Whether [midi] sits between two degrees of the scale of [key]. */
fun isRaisedOf(midi: Int, key: Key) = (midi - key.majorTonic.value).mod(12) in RAISED_SEMITONES

/** The staff step [drum] is written on, counted from C of octave 0. */
fun staffStepOf(drum: Drum) = when (drum) {
    Drum.Kick -> 38
    Drum.Snare -> 42
    Drum.Tom -> 44
    Drum.Ride -> 45
    Drum.HiHat -> 46
    Drum.Crash -> 47
    Drum.Sticks -> 48
}

/** The head [drum] is written with. */
fun headShapeOf(drum: Drum) = when (drum) {
    Drum.Kick, Drum.Snare, Drum.Tom -> HeadShape.Round
    Drum.HiHat, Drum.Crash, Drum.Ride, Drum.Sticks -> HeadShape.Cross
}

/** The ledger lines a note reaching from [lowest] to [highest] wants, one on each line between it and the staff. */
fun ledgersOf(lowest: Int, highest: Int) =
    (STAFF_HIGH + 2..highest step 2).toList() + (STAFF_LOW - 2 downTo lowest step 2).toList()

/** The measures this span fills, [measureTicks] long, the first downbeat of the source leading with the pickup. */
fun NotationSpan.measureLineOf(measureTicks: Int): MeasureLine {
    // measure 0 is the pickup, so the span's first measure follows the measures before it
    val first = start / measureTicks + 1
    val end = first + (ticks + measureTicks - 1) / measureTicks
    return MeasureLine(if (first == 1) 0 else first, end)
}
