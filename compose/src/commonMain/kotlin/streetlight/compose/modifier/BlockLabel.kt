package streetlight.compose.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.shadow.DropShadowPainter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints

/**
 * Labels the content with [label] on a pill over its top edge, two and a half units from its end. The label's background tells
 * whether the part is required and missing, while [isValid] is `false`, or required and given, while it is `true`. The
 * cousin of the web's `blockLabel` and `flowValid`.
 */
fun Modifier.blockLabel(label: String, isValid: Boolean? = null) = this then BlockLabelElement(label, isValid)

private data class BlockLabelElement(val label: String, val isValid: Boolean?) : ModifierNodeElement<BlockLabelNode>() {
    override fun create() = BlockLabelNode(label, isValid)
    override fun update(node: BlockLabelNode) {
        node.label = label
        node.isValid = isValid
        node.invalidateDraw()
    }
}

private class BlockLabelNode(var label: String, var isValid: Boolean?) :
    Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
    private var measurer: TextMeasurer? = null
    private var shadows: List<DropShadowPainter>? = null
    private var shadowCurve: Float? = null

    override fun ContentDrawScope.draw() {
        drawContent()
        val theme = theme
        val colors = theme.colors
        val unit = theme.unit.toPx()
        val measurer = measurer ?: TextMeasurer(
            currentValueOf(LocalFontFamilyResolver),
            currentValueOf(LocalDensity),
            currentValueOf(LocalLayoutDirection),
        ).also { measurer = it }
        val curve = theme.curve
        val shadows = shadows?.takeIf { shadowCurve == curve } ?: LabelShadows.map {
            requireGraphicsContext().shadowContext.createDropShadowPainter(theme.pillShape, it)
        }.also {
            shadows = it
            shadowCurve = curve
        }

        // the web's padding of .12rem .5rem .1rem, no wider than 75% of the content
        val padX = unit * PadXUnits
        val maxTextWidth = (size.width * MaxWidthFraction - padX * 2).toInt().coerceAtLeast(0)
        val text = measurer.measure(
            label.uppercase(),
            theme.text.label.copy(color = colors.ink.copy(alpha = InkAlpha)),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            constraints = Constraints(maxWidth = maxTextWidth),
        )
        val labelSize = Size(text.size.width + padX * 2, text.size.height + unit * (PadTopUnits + PadBottomUnits))
        // centered on the outline, which runs a line's width inside the top edge
        val topLeft = Offset(size.width - unit * EndUnits - labelSize.width, (theme.line.toPx() - labelSize.height) / 2)
        val background = when (isValid) {
            null -> colors.voidBorder
            false -> colors.requiredBg
            true -> colors.validBg
        }
        translate(topLeft.x, topLeft.y) {
            shadows.forEach { with(it) { draw(labelSize) } }
            drawRoundRect(background, size = labelSize, cornerRadius = CornerRadius(labelSize.height / 2 * curve))
            drawText(text, topLeft = Offset(padX, unit * PadTopUnits))
        }
    }
}

/** The web's `--input-shadow`. */
private val LabelShadows = listOf(
    Shadow(radius = 2.dp, color = Color.Black.copy(alpha = 0.4f), offset = DpOffset(0.dp, 1.dp)),
    Shadow(radius = 6.dp, color = Color.Black.copy(alpha = 0.3f)),
)

/** The label's padding in units, the web's `.12rem .5rem .1rem`, and its inset from the end. */
private const val PadTopUnits = 0.24f
private const val PadBottomUnits = 0.2f
private const val PadXUnits = 1f
private const val EndUnits = 2.5f

private const val MaxWidthFraction = 0.75f
private const val InkAlpha = 0.8f
