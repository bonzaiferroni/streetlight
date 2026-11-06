package streetlight.web.model

import streetlight.model.data.SectionId
import streetlight.model.data.TimeSignature
import streetlight.model.data.Track
import streetlight.model.data.lengthOf

/** The notation of [sectionId] in a track, [ticks] long, with [measures] the share of it each measure fills. */
data class SectionNotation(
    val sectionId: SectionId,
    val ticks: Int,
    val measures: List<Double>,
)

/** The notation of [sectionId] in [track], cut into measures by [time], or `null` when the track gives it no length. */
fun sectionNotationOf(track: Track, sectionId: SectionId, time: TimeSignature): SectionNotation? {
    val ticks = track.lengthOf(sectionId)?.ticks?.takeIf { it > 0 } ?: return null
    val measure = time.measure.ticks
    val count = (ticks + measure - 1) / measure
    val measures = List(count) { index -> minOf(measure, ticks - index * measure).toDouble() / ticks }
    return SectionNotation(sectionId, ticks, measures)
}

/** The notation of each section and arrangement [track] defines, cut into measures by [time]. */
fun sectionNotationsOf(track: Track, time: TimeSignature) =
    (track.sections.map { it.sectionId } + track.arrangements.map { it.sectionId })
        .mapNotNull { sectionNotationOf(track, it, time) }
