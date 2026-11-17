package streetlight.model.music.ui

import streetlight.model.music.Music
import streetlight.model.music.NotationId
import streetlight.model.music.SectionId
import streetlight.model.music.Track
import streetlight.model.music.nameOf
import streetlight.model.music.notationsOf
import streetlight.model.music.repeatsOf

/**
 * A section of the song as an overview draws it, [sectionId] [measures] long in the song, named [name]. A track's row
 * plays the notation or arrangement [notationId] there as its [passes], and rests through what is left.
 */
data class SectionCell(
    val sectionId: SectionId,
    val name: String,
    val measures: Int,
    val passes: List<CellPass> = emptyList(),
    val notationId: NotationId? = null,
)

/** A notation [notationId] played through once in a cell, [measures] long, shown as [name]. */
data class CellPass(
    val notationId: NotationId,
    val name: String,
    val measures: Double,
)

/** The cells of the sections [music] plays, in order, each under its name. */
fun cellsOf(music: Music): List<SectionCell> = music.arrangement.map { sectionId ->
    val measures = music.sections.firstOrNull { it.sectionId == sectionId }?.measures ?: 0
    SectionCell(sectionId, music.nameOf(sectionId), measures)
}

/**
 * The cells of the sections [music] plays, in order, as [track] plays each: the notation or arrangement it plays
 * there, an arrangement resolved into the notations it holds, repeated as many whole times as fit.
 */
fun cellsOf(music: Music, track: Track): List<SectionCell> {
    val measureTicks = music.time.measure.ticks.toDouble()
    return cellsOf(music).map { cell ->
        val notationId = track.sections[cell.sectionId] ?: return@map cell
        val notations = track.notationsOf(notationId) ?: return@map cell
        val span = music.time.measure * cell.measures
        val passes = notations.map { CellPass(it.notationId, track.nameOf(it.notationId, music), it.length.ticks / measureTicks) }
        cell.copy(
            passes = List(track.repeatsOf(notationId, span)) { passes }.flatten(),
            notationId = notationId,
        )
    }
}
