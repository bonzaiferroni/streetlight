package streetlight.model.music.ui

import streetlight.model.music.Music
import streetlight.model.music.SectionId
import streetlight.model.music.Track
import streetlight.model.music.lengthOf
import streetlight.model.music.repeatsOf

/**
 * A section of the song as an overview draws it, [sectionId] shown as [name] and [measures] long in the song. Its row
 * plays it [repeats] times of [repeatMeasures] each and rests through what is left, or rests through it all at none.
 */
data class SectionCell(
    val sectionId: SectionId,
    val name: String,
    val measures: Int,
    val repeats: Int,
    val repeatMeasures: Double,
)

/** The cells of the sections [music] plays, in order, each played once through by the song itself. */
fun cellsOf(music: Music): List<SectionCell> = music.arrangement.map { sectionId ->
    val measures = music.sections.firstOrNull { it.sectionId == sectionId }?.measures ?: 0
    SectionCell(sectionId, music.nameOf(sectionId), measures, 1, measures.toDouble())
}

/** The cells of the sections [music] plays, in order, as [track] plays each. */
fun cellsOf(music: Music, track: Track): List<SectionCell> {
    val measureTicks = music.time.measure.ticks.toDouble()
    return cellsOf(music).map { cell ->
        val notationId = track.sections[cell.sectionId]
        val length = notationId?.let { track.lengthOf(it) } ?: return@map cell.copy(repeats = 0, repeatMeasures = 0.0)
        val span = music.time.measure * cell.measures
        cell.copy(repeats = track.repeatsOf(notationId, span), repeatMeasures = length.ticks / measureTicks)
    }
}
