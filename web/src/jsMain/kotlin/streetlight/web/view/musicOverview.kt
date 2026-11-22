package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.model.HeadingLevel
import koala.modifier.*
import kotlinx.css.Flex
import web.html.HTMLElement
import streetlight.model.music.Track
import streetlight.model.music.TrackType
import streetlight.model.music.ui.NotationCell
import streetlight.model.music.ui.SectionCell
import streetlight.web.dom.layout.actionSection
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicEditorFocus
import streetlight.web.model.NotationFocus

/**
 * Renders the overview of the song under its heading: a row of the sections it plays, then a row for each track,
 * where a pass hands [setFocus] the notation it plays.
 */
context(model: MusicEditor)
fun ViewScope.musicTable(setFocus: (MusicEditorFocus?) -> Unit) {
    actionSection("Overview", level = HeadingLevel.H4) {
        flowBlock(model.tableState, OverviewStyle.Grid) { overview ->
            // the grid's rows are its children, so the block that rebuilds them holds the grid
            sectionRow(overview.sectionCells, null) { }
            overview.trackCells.forEach { (track, cells) ->
                sectionRow(cells, track.trackType, { pass -> setFocus(NotationFocus(track.trackId, pass.notationId)) }) { trackLabel(track) }
            }
        }
    }
}

/**
 * Renders a row of an overview grid: [label] in the grid's first column, then a group for each of [cells] across the
 * second, as wide as its share of the song's measures, holding a cell for each notation it passes through, drawn in
 * the color of [trackType], and one for its rest. A notation's cell hands its pass to [onPass] when clicked.
 */
fun ViewScope.sectionRow(
    cells: List<SectionCell>,
    trackType: TrackType?,
    onPass: ((NotationCell) -> Unit)? = null,
    label: ViewScope.() -> Unit,
) {
    div(null) { label() }
    div(OverviewStyle.Cells) {
        cells.forEach { cell ->
            div(modify(OverviewStyle.Section, Css.Flex.of(Flex(cell.measures)))) { sectionCells(cell, trackType, onPass) }
        }
    }
}

/** Renders the label of [track] in an overview: its icon and name. */
fun ViewScope.trackLabel(track: Track) = row(AlignItemsCenter) {
    icon(track.trackType.svg.small, Height(3))
    textBlock(track.name, modify(TextSmall, SingleLine))
}

/**
 * Renders the cells of [cell]: the section whole in the song's own row, or a cell for each pass of a track, in the
 * color of [trackType] and handed to [onPass] when clicked, then one for its rest.
 */
private fun ViewScope.sectionCells(cell: SectionCell, trackType: TrackType?, onPass: ((NotationCell) -> Unit)?) {
    if (trackType == null) return overviewCell(cell.name, cell.measures.toDouble(), ZenBg)

    // a notation that runs past its section is drawn to the section's end
    var drawn = 0.0
    cell.passes.forEach { pass ->
        val measures = minOf(pass.measures, cell.measures - drawn).takeIf { it > 0 } ?: return@forEach
        drawn += measures
        val onClick = onPass?.let { { it(pass) } }
        overviewCell(pass.name, measures, trackType.color.bg, onClick, transition("card-${pass.notationId}"))
    }
    val rest = cell.measures - drawn
    if (rest > 0) overviewCell(NO_BREAK_SPACE, rest, OverviewStyle.Rest)
}

/**
 * Renders a cell [measures] long, showing its [name] on a ground drawn with [groundMod], which runs [onClick] when
 * clicked. A clicked cell takes [transitionMod] as it runs it, so the card it opens grows out of it alone, however
 * many cells play the same notation.
 */
private fun ViewScope.overviewCell(
    name: String,
    measures: Double,
    groundMod: Modifier,
    onClick: (() -> Unit)? = null,
    transitionMod: Modifier? = null,
) {
    val cellMod = modify(OverviewStyle.Cell, groundMod, Css.Flex.of(Flex(measures)))
    if (onClick == null) {
        div(cellMod) { textBlock(name, modify(TextSmall, SingleLine)) }
        return
    }
    lateinit var element: HTMLElement
    element = button({
        transitionMod?.let { element.modify(it) }
        onClick()
    }, cellMod) {
        textBlock(name, modify(TextSmall, SingleLine))
    }
}

/** A label that keeps the line of a cell's name where a rest has none. */
private const val NO_BREAK_SPACE = " "
