package streetlight.model.music.ui

import streetlight.model.music.Beats
import streetlight.model.music.Chord
import streetlight.model.music.Drum
import streetlight.model.music.Key
import streetlight.model.music.NotationId
import streetlight.model.music.TimeSignature
import streetlight.model.music.TrackId
import streetlight.model.music.TrackType

data class MusicSource(
    val name: String,
    val pickup: Beats,
    val measureCount: Int,
    val tracks: List<TrackSource>,
    val time: TimeSignature,
    val key: Key,
    val bpm: Int,
    val capo: Int?,
) {
    /** The ticks from the first downbeat to the end. */
    val duration get() = measureCount * time.measure.ticks

    val secondsPerTick get() = 60.0 / bpm / time.pulse.ticks

    /** The tick measure [index] starts at, measure 0 the pickup that ends at the first downbeat. */
    fun measureTickOf(index: Int) = if (index == 0) -pickup.ticks else (index - 1) * time.measure.ticks

    /** The beats of measure [index], measure 0 the pickup. */
    fun measureBeatsOf(index: Int) = if (index == 0) pickup else time.measure
}

data class TrackSource(
    val trackId: TrackId,
    val name: String,
    val events: List<NotationEvent>,
    val measures: List<MeasureSpan>,
    val passes: List<NotationPass>,
    val layout: ProtoNotationLayout,
    val trackType: TrackType,
    val capo: Int?,
)

/* representation invariants:
-events are sorted by tick
-a track's measures run parallel to the grid: index 0 the pickup (empty when there is none), then measureCount measures
-a track's measures tile its events by index, each event in the measure that holds its tick
-lines tile the measures the track plays, by index
 */

sealed interface NotationEvent {
    val tick: Int
    val beats: Beats
    val notationId: NotationId
    val index: Int
}

data class NoteEvent(
    val midi: Int?,
    override val tick: Int,
    override val beats: Beats,
    override val notationId: NotationId,
    override val index: Int,
): NotationEvent

data class DrumEvent(
    val drum: Drum?,
    override val tick: Int,
    override val beats: Beats,
    override val notationId: NotationId,
    override val index: Int,
): NotationEvent {
}

data class ChordEvent(
    val chord: Chord?,
    override val tick: Int,
    override val beats: Beats,
    override val notationId: NotationId,
    override val index: Int,
): NotationEvent

data class VocalEvent(
    val text: String?,
    override val tick: Int,
    override val beats: Beats,
    override val notationId: NotationId,
    override val index: Int,
): NotationEvent

data class ProtoNotationLayout(
    val lines: List<NotationLayoutLine>,
    val window: StaffWindow,
)

data class NotationLayoutLine(
    val measureStart: Int,
    val measureEnd: Int,
)

data class MeasureSpan(
    val eventStart: Int,
    val eventEnd: Int,
)