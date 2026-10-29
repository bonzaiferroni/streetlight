package streetlight.compose.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import streetlight.model.data.EntityDetail
import streetlight.compose.model.currentPortal
import streetlight.compose.model.toAppRoute
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.padding

/**
 * The [details] of an entity as cells in as many equal columns of at least [MinCellWidth] as fit, up to [MaxColumns],
 * wrapping onto more lines as needed. The last cell stretches to the end of its line, so the details can save a wide
 * fact for last. The cells stand a line apart, so the background between them draws the lines of the grid. The cousin
 * of the web's `cellGrid`.
 */
@Composable
fun CellGrid(details: List<EntityDetail>, modifier: Modifier = Modifier) {
    if (details.isEmpty()) return
    val theme = currentTheme

    Layout(
        content = { details.forEach { Cell(it) } },
        modifier = modifier,
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val gap = theme.line.roundToPx()
        val cellHeight = (theme.unit * CellUnits).roundToPx()
        val columns = ((width + gap) / (MinCellWidth.roundToPx() + gap)).coerceIn(1, MaxColumns)

        // the columns share the width evenly, the pixels left over going one each to the first columns
        val space = width - (columns - 1) * gap
        val columnWidths = List(columns) { space / columns + if (it < space % columns) 1 else 0 }
        val columnStarts = columnWidths.runningFold(0) { x, columnWidth -> x + columnWidth + gap }

        val placed = measurables.mapIndexed { index, measurable ->
            val column = index % columns
            val x = columnStarts[column]
            val cellWidth = if (index == measurables.lastIndex) width - x else columnWidths[column]
            measurable.measure(Constraints.fixed(cellWidth, cellHeight)) to x
        }
        val lines = (measurables.size + columns - 1) / columns
        val height = lines * cellHeight + (lines - 1) * gap
        layout(width, height) {
            placed.forEachIndexed { index, (placeable, x) ->
                placeable.place(x, index / columns * (cellHeight + gap))
            }
        }
    }
}

/**
 * A cell: the detail's icon, or else its label, in its color or the scheme color at the start, then its text centered
 * in the rest. A cell opens the app's page of its route when clicked, or else its url when it leaves the app.
 */
@Composable
private fun Cell(detail: EntityDetail) {
    val theme = currentTheme
    val color = detail.type?.let { theme.colors.colorOf(it) } ?: LocalSchemeColor.current
    val uriHandler = LocalUriHandler.current
    val portal = currentPortal
    val route = detail.route?.toAppRoute()
    val url = detail.url?.value?.takeIf { it.startsWith("http") }
    val onClick = when {
        route != null -> { -> portal.go(route) }
        url != null -> { -> uriHandler.openUri(url) }
        else -> null
    }
    Row(
        modifier = Modifier.fillMaxSize()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .cardBg()
            .padding(1),
        horizontalArrangement = Arrangement.spacedBy(IconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val svg = detail.svg
        val label = detail.label
        when {
            svg != null -> Icon(svg = svg, color = { color })
            label != null -> BasicText(
                text = label,
                style = theme.text.small,
                color = { color },
                maxLines = 1,
            )
        }
        BasicText(
            text = detail.text,
            modifier = Modifier.weight(1f),
            style = theme.text.small.copy(textAlign = TextAlign.Center),
            color = { theme.colors.ink },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The narrowest column of the grid: the web's 128px. */
private val MinCellWidth = 128.dp

/** The most columns the grid takes: the web's 7. */
private const val MaxColumns = 7

/** The height of a cell in units. */
private const val CellUnits = 5

/** The space between a cell's icon and its text: the web's 4px. */
private val IconGap = 4.dp
