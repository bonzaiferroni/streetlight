package streetlight.compose.modifier

// the cousins of the web's LayoutCss: spacing in units

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.offset

/** Pads every side by [all] units of the theme's unit. The web's `padding: var(--unit)`. */
fun Modifier.padding(all: Number) = padding(all, all, all, all)

/** Pads the sides by [horizontal] units and the top and bottom by [vertical] units. The web's `PaddingX1`, `PaddingY1`. */
fun Modifier.padding(horizontal: Number = 0, vertical: Number = 0) = padding(horizontal, vertical, horizontal, vertical)

/** Pads each side by its own count of units, [start] and [end] following the layout direction. */
fun Modifier.padding(start: Number = 0, top: Number = 0, end: Number = 0, bottom: Number = 0) =
    this then PaddingElement(start.toFloat(), top.toFloat(), end.toFloat(), bottom.toFloat())

private data class PaddingElement(val start: Float, val top: Float, val end: Float, val bottom: Float) :
    ModifierNodeElement<PaddingNode>() {
    override fun create() = PaddingNode(start, top, end, bottom)
    override fun update(node: PaddingNode) {
        node.start = start
        node.top = top
        node.end = end
        node.bottom = bottom
        node.invalidateMeasurement()
    }
}

private class PaddingNode(var start: Float, var top: Float, var end: Float, var bottom: Float) :
    Modifier.Node(), LayoutModifierNode, CompositionLocalConsumerModifierNode {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val unit = theme.unit
        val startPx = (unit * start).roundToPx()
        val topPx = (unit * top).roundToPx()
        val horizontal = startPx + (unit * end).roundToPx()
        val vertical = topPx + (unit * bottom).roundToPx()
        val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))
        val width = constraints.constrainWidth(placeable.width + horizontal)
        val height = constraints.constrainHeight(placeable.height + vertical)
        return layout(width, height) { placeable.placeRelative(startPx, topPx) }
    }
}
