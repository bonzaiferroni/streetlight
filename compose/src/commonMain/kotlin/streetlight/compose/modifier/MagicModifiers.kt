package streetlight.compose.modifier

// the cousins of the web's MagicCss: motion that dresses its content

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameNanos
import kotlin.random.Random

/**
 * Centers the content in the width it is given and draws a rule on either side of it, each at most 16 units long, with
 * the particle rays of the web's `filigree` drifting outward along it.
 */
fun Modifier.filigree() = this then FiligreeElement

private data object FiligreeElement : ModifierNodeElement<FiligreeNode>() {
    override fun create() = FiligreeNode()
    override fun update(node: FiligreeNode) = Unit
}

private class FiligreeNode : Modifier.Node(), LayoutModifierNode, DrawModifierNode, CompositionLocalConsumerModifierNode {
    // each rule has its own seed, as each of the web's rules sets its own
    private val seeds = floatArrayOf(Random.nextFloat(), Random.nextFloat())
    private var time = 0f
    private var contentLeft = 0
    private var contentWidth = 0
    private var width = 0

    override fun onAttach() {
        coroutineScope.launch {
            val start = withFrameNanos { it }
            while (isActive) withFrameNanos {
                time = (it - start) / 1_000_000_000f
                invalidateDraw()
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints.copy(minWidth = 0))
        width = if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.width
        contentLeft = (width - placeable.width) / 2
        contentWidth = placeable.width
        return layout(width, placeable.height) { placeable.place(contentLeft, 0) }
    }

    // a node that places its content draws in the content's space, so the rules reach out from the content's edges
    override fun ContentDrawScope.draw() {
        drawContent()
        val unit = theme.unit.toPx()
        val line = theme.line.toPx()
        val longest = unit * RuleUnits
        val color = theme.colors.ink
        val top = (size.height - line) / 2

        val leftLength = (contentLeft - unit).coerceAtMost(longest)
        if (leftLength > 0) drawRule(Rect(-unit - leftLength, top, -unit, top + line), color, seeds[0], outwardLeft = true)

        val rightStart = contentWidth + unit
        val rightLength = (width - contentLeft - rightStart).coerceAtMost(longest)
        if (rightLength > 0) drawRule(Rect(rightStart, top, rightStart + rightLength, top + line), color, seeds[1], outwardLeft = false)
    }

    /** A rule in [color] at half opacity, its rays drifting away from the content, toward the left when [outwardLeft]. */
    private fun DrawScope.drawRule(rule: Rect, color: Color, seed: Float, outwardLeft: Boolean) {
        drawRect(color.copy(alpha = RuleAlpha * RuleOpacity), rule.topLeft, rule.size)
        clipRect(rule.left, rule.top, rule.right, rule.bottom) {
            rays.forEach { (duration, delay) ->
                val elapsed = time - (delay + seed)
                if (elapsed < 0f) return@forEach
                val progress = elapsed % (duration + seed) / (duration + seed)
                // the ray crosses from a rule's length before it to a rule's length past it, as translateX(-100% to 100%)
                val shift = (progress * 2 - 1) * rule.width
                val start = if (outwardLeft) rule.left - shift else rule.left + shift
                drawRect(
                    Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.5f to color.copy(alpha = RayAlpha * RuleOpacity),
                        1f to Color.Transparent,
                        startX = start,
                        endX = start + rule.width,
                    ),
                    Offset(start, rule.top),
                    Size(rule.width, rule.height),
                )
            }
        }
    }
}

/** The longest a rule runs, in units: the web's `MaxWidth(16)`. */
private const val RuleUnits = 16

/** The rule's own opacity, under its rays, and the opacity of a ray: the web's `opacity: .8`. */
private const val RuleAlpha = 0.35f
private const val RayAlpha = 0.8f

/** The web's `OpacityHalf` over the whole rule. */
private const val RuleOpacity = 0.5f

/** Each ray's duration and delay in seconds, before the rule's seed is added: the web's `::before` and `::after`. */
private val rays = listOf(4f to 2f, 2f to 2f)
