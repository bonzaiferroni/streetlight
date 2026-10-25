package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import streetlight.compose.model.EntityDetail
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.opacityLow

/**
 * The details of an entity as cells: the [top] details and the [bottom] details on one line when every cell gets at
 * least [MinCellWidth], otherwise the top line over the bottom. The cells of a line share its width evenly, a line
 * apart, so the background between them draws the lines of the grid. The cousin of the web's `cellGrid`.
 */
@Composable
fun CellGrid(top: List<EntityDetail>, bottom: List<EntityDetail>, modifier: Modifier = Modifier) {
    if (top.isEmpty() && bottom.isEmpty()) return
    val theme = currentTheme
    val cells = top + bottom

    Layout(
        content = { cells.forEach { Cell(it) } },
        modifier = modifier.borderRadius(2.5, moonShadow = true, outline = true),
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val gap = theme.line.roundToPx()
        val cellHeight = (theme.unit * CellUnits).roundToPx()
        val fitsOneLine = width >= cells.size * MinCellWidth.roundToPx() + (cells.size - 1) * gap
        val lines = if (fitsOneLine) listOf(measurables) else listOf(measurables.take(top.size), measurables.drop(top.size))
            .filter { it.isNotEmpty() }

        // each line shares its width evenly, the pixels left over going one each to its first cells
        val placed = lines.map { line ->
            val space = width - (line.size - 1) * gap
            line.mapIndexed { index, measurable ->
                val cellWidth = space / line.size + if (index < space % line.size) 1 else 0
                measurable.measure(Constraints.fixed(cellWidth, cellHeight))
            }
        }
        val height = lines.size * cellHeight + (lines.size - 1) * gap
        layout(width, height) {
            placed.forEachIndexed { lineIndex, line ->
                var x = 0
                line.forEach { placeable ->
                    placeable.place(x, lineIndex * (cellHeight + gap))
                    x += placeable.width + gap
                }
            }
        }
    }
}

/**
 * A cell: the detail's icon in its color at the start, then its text centered in the rest; the icon alone at low
 * opacity when the entity lacks it, so every cell's icon stands in the same place.
 */
@Composable
private fun Cell(detail: EntityDetail) {
    val theme = currentTheme
    val text = detail.text
    Row(
        modifier = Modifier.fillMaxSize().cardBg().padding(theme.unit),
        horizontalArrangement = Arrangement.spacedBy(IconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            svg = detail.svg,
            color = { theme.colors.colorOf(detail.color) },
            modifier = if (text == null) Modifier.opacityLow() else Modifier,
        )
        if (text != null) {
            BasicText(
                text = text,
                modifier = Modifier.weight(1f),
                style = theme.text.small.copy(textAlign = TextAlign.Center),
                color = { theme.colors.ink },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The narrowest a cell may be on a line of all the details: the web's column of 128px. */
private val MinCellWidth = 128.dp

/** The height of a cell in units. */
private const val CellUnits = 5

/** The space between a cell's icon and its text: the web's 4px. */
private val IconGap = 4.dp
