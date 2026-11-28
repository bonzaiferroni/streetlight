package streetlight.model.music.ui

import streetlight.model.music.Beats
import streetlight.model.music.Chord
import streetlight.model.music.Drum
import streetlight.model.music.DrumHit
import streetlight.model.music.Key
import streetlight.model.music.NotationId
import streetlight.model.music.SectionId
import streetlight.model.music.TimeSignature
import streetlight.model.music.TrackId
import streetlight.model.music.TrackType

/**
 * The target [targetId] of a song, the whole song when `null`, unfolded into [tracks] over a grid of [measureCount]
 * measures after a [pickup], tick 0 at the target's first downbeat.
 */
data class MusicSource(
    val name: String,
    val targetId: NotationId?,
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

/**
 * A track of a [MusicSource], as its parts:
 * - [events], sorted by tick
 * - [measures], parallel to the grid: index 0 the pickup, empty when there is none, then a span for each measure,
 *   together tiling [events] by index, each event in the measure that holds its tick
 * - [notations], in order of start, each arrangement ahead of the spans it holds, which fall within its ticks; those that
 *   are not arrangements tile the ticks the track plays, back to back but for an arrangement's wait
 */
data class TrackSource(
    val trackId: TrackId,
    val name: String,
    val events: List<NotationEvent>,
    val measures: List<MeasureSpan>,
    val notations: List<NotationSpan>,
    val layout: NotationLayout,
    val trackType: TrackType,
    val capo: Int?,
)

sealed interface NotationEvent {
    val tick: Int
    val beats: Beats
    val notationId: NotationId
    val index: Int
    val sectionId: SectionId?
}

data class NoteEvent(
    val midi: Int?,
    override val tick: Int,
    override val beats: Beats,
    override val notationId: NotationId,
    override val index: Int,
    override val sectionId: SectionId?,
): NotationEvent

data class DrumEvent(
    val drum: Drum,
    val hit: DrumHit?,
    override val tick: Int,
    override val beats: Beats,
    override val notationId: NotationId,
    override val index: Int,
    override val sectionId: SectionId?,
): NotationEvent

data class ChordEvent(
    val chord: Chord?,
    override val tick: Int,
    override val beats: Beats,
    override val notationId: NotationId,
    override val index: Int,
    override val sectionId: SectionId?,
): NotationEvent

data class VocalEvent(
    val text: String?,
    override val tick: Int,
    override val beats: Beats,
    override val notationId: NotationId,
    override val index: Int,
    override val sectionId: SectionId?,
): NotationEvent

/** The [lines] a track is drawn in, tiling the measures it plays by index, within [window]. */
data class NotationLayout(
    val lines: List<MeasureLine>,
    val window: StaffWindow,
)

data class MeasureLine(
    val measureStart: Int,
    val measureEnd: Int,
)

data class MeasureSpan(
    val eventStart: Int,
    val eventEnd: Int,
)

/**
 * A time a track plays [notationId], shown as [label], for [ticks] from its first downbeat at [start], with [pickup]
 * before it, as section [sectionId] when it plays within one, an arrangement when [isArrangement].
 */
data class NotationSpan(
    val notationId: NotationId,
    val label: String,
    val start: Int,
    val ticks: Int,
    val pickup: Beats,
    val isArrangement: Boolean,
    val sectionId: SectionId? = null,
) {
    /** Whether [event] is one this span plays, its pickup included. */
    fun holds(event: NotationEvent) = event.notationId == notationId && event.tick in start - pickup.ticks until start + ticks
}
