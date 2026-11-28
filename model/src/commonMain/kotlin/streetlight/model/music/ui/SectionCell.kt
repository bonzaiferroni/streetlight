package streetlight.model.music.ui

import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.SectionId
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.nameOf
import streetlight.model.music.notationsOf
import streetlight.model.music.trackOrNullOf

/** The cells of a song in an overview: its [sectionCells], then the [trackCells] of each track. */
data class MusicTable(
    val sectionCells: List<SectionCell> = emptyList(),
    val trackCells: List<TrackCells> = emptyList(),
)

/** The [cells] of [track] in an overview of its song. */
data class TrackCells(
    val track: Track,
    val cells: List<SectionCell>,
)

fun MusicSheet.getMusicTable() =
    MusicTable(getSectionCells(), tracks.map { TrackCells(it, notationCellsOf(it.trackId)) })

/**
 * A section of the song as an overview draws it, [sectionId] [measures] long in the song, named [name]. A track's row
 * plays the notation or arrangement [notationId] there as its [passes], and rests through what is left.
 */
data class SectionCell(
    val sectionId: SectionId,
    val name: String,
    val measures: Int,
    val passes: List<NotationCell> = emptyList(),
    val notationId: NotationId? = null,
)

/** A notation [notationId] played through once in a cell, [measures] long, shown as [name]. */
data class NotationCell(
    val notationId: NotationId,
    val name: String,
    val measures: Double,
)

fun MusicSheet.getSectionCells() = music.arrangement.map { sectionId ->
    val measures = music.sections.firstOrNull { it.sectionId == sectionId }?.measures ?: 0
    SectionCell(sectionId, music.nameOf(sectionId), measures)
}

fun MusicSheet.notationCellsOf(trackId: TrackId): List<SectionCell> {
    val measureTicks = music.time.measure.ticks.toDouble()
    val track = trackOrNullOf(trackId) ?: return emptyList()
    return getSectionCells().map { cell ->
        val notationId = track.sections[cell.sectionId] ?: return@map cell
        val notations = track.notationsOf(notationId) ?: return@map cell
        val passes = notations.map { NotationCell(it.notationId, track.nameOf(it.notationId), it.length.ticks / measureTicks) }
        cell.copy(passes = passes, notationId = notationId)
    }
}
