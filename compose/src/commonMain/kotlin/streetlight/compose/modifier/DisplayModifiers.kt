package streetlight.compose.modifier

// the cousins of the web's DisplayCss: borders, shadows and opacity

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.DropShadowPainter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.invalidateLayer
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Rounds the corners to [units] of the theme's unit and clips the content to them. The web's `BorderRadius(n)`. */
fun Modifier.borderRadius(units: Int) = this then BorderRadiusElement(units)

/** Draws the theme's low outline inside the edge, its corners rounded to [radius] units. The web's `--outline-low`. */
fun Modifier.outline(radius: Int = 0) = this then OutlineElement(radius)

/** Casts the soft glow the web calls `MoonShadow` behind the content, its corners rounded to [radius] units. */
fun Modifier.moonShadow(radius: Int = 0) = this then MoonShadowElement(radius)

/** Paints the theme's quiet background behind the content. The web's `ZenBg`. */
fun Modifier.zenBg() = this then BackgroundElement(Background.Zen)

/** Paints the theme's card background behind the content, a step firmer than [zenBg]. The web's `CardBg`. */
fun Modifier.cardBg() = this then BackgroundElement(Background.Card)

/**
 * Clips the content at [height] units and fades it over the two units that end [inset] units above the clip, as the
 * web's text clip with its mask of fixed stops: only content that reaches the clip fades.
 */
fun Modifier.fadeClip(height: Int, inset: Float = 0f) = this then FadeClipElement(height, inset)

/** Draws the content at the theme's low opacity. The web's `OpacityLow`. */
fun Modifier.opacityLow() = this then OpacityElement(Opacity.Low)

/** Draws the content at the theme's high opacity, a step quieter than full. The web's `OpacityHigh`. */
fun Modifier.opacityHigh() = this then OpacityElement(Opacity.High)

/** The theme's opacity levels a modifier applies. */
private enum class Opacity { Low, High }

private data class BorderRadiusElement(val units: Int) : ModifierNodeElement<BorderRadiusNode>() {
    override fun create() = BorderRadiusNode(units)
    override fun update(node: BorderRadiusNode) {
        node.units = units
        node.invalidateLayer()
    }
}

private class BorderRadiusNode(var units: Int) : Modifier.Node(), LayoutModifierNode, CompositionLocalConsumerModifierNode {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val corner = theme.unit * units
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                shape = RoundedCornerShape(corner)
                clip = true
            }
        }
    }
}

private data class OutlineElement(val radius: Int) : ModifierNodeElement<OutlineNode>() {
    override fun create() = OutlineNode(radius)
    override fun update(node: OutlineNode) {
        node.radius = radius
        node.invalidateDraw()
    }
}

private class OutlineNode(var radius: Int) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
    override fun ContentDrawScope.draw() {
        drawContent()
        val width = theme.line.toPx()
        val corner = (theme.unit * radius).toPx()
        // the stroke runs inside the edge, its corners following the edge's, as a CSS border does
        drawRoundRect(
            color = theme.colors.outlineLow,
            topLeft = Offset(width / 2, width / 2),
            size = Size(size.width - width, size.height - width),
            cornerRadius = CornerRadius((corner - width / 2).coerceAtLeast(0f)),
            style = Stroke(width),
        )
    }
}

private data class MoonShadowElement(val radius: Int) : ModifierNodeElement<MoonShadowNode>() {
    override fun create() = MoonShadowNode(radius)
    override fun update(node: MoonShadowNode) {
        node.radius = radius
        node.painters = null
        node.invalidateDraw()
    }
}

private class MoonShadowNode(var radius: Int) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
    var painters: List<DropShadowPainter>? = null
    private var paintedUnit: Dp? = null

    override fun ContentDrawScope.draw() {
        val unit = theme.unit
        val shadows = painters?.takeIf { paintedUnit == unit } ?: moonShadows.map { shadow ->
            requireGraphicsContext().shadowContext.createDropShadowPainter(RoundedCornerShape(unit * radius), shadow)
        }.also {
            painters = it
            paintedUnit = unit
        }
        shadows.forEach { with(it) { draw(size) } }
        drawContent()
    }
}

/** The theme's backgrounds a modifier paints. */
private enum class Background { Zen, Card }

private data class BackgroundElement(val background: Background) : ModifierNodeElement<BackgroundNode>() {
    override fun create() = BackgroundNode(background)
    override fun update(node: BackgroundNode) {
        node.background = background
        node.invalidateDraw()
    }
}

private class BackgroundNode(var background: Background) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
    override fun ContentDrawScope.draw() {
        val color = when (background) {
            Background.Zen -> theme.colors.zenBg
            Background.Card -> theme.colors.cardBg
        }
        drawRect(color)
        drawContent()
    }
}

private data class FadeClipElement(val height: Int, val inset: Float) : ModifierNodeElement<FadeClipNode>() {
    override fun create() = FadeClipNode(height, inset)
    override fun update(node: FadeClipNode) {
        node.height = height
        node.inset = inset
        node.invalidateMeasurement()
    }
}

private class FadeClipNode(var height: Int, var inset: Float) :
    Modifier.Node(), LayoutModifierNode, DrawModifierNode, CompositionLocalConsumerModifierNode {
    private var clipHeight = 0f

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val clipPx = (theme.unit * height).roundToPx()
        clipHeight = clipPx.toFloat()
        val maxHeight = minOf(constraints.maxHeight, clipPx)
        val placeable = measurable.measure(constraints.copy(minHeight = minOf(constraints.minHeight, maxHeight), maxHeight = maxHeight))
        return layout(placeable.width, placeable.height) {
            // an offscreen layer, so the fade masks the content alone
            placeable.placeWithLayer(0, 0) {
                clip = true
                compositingStrategy = CompositingStrategy.Offscreen
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val unit = theme.unit.toPx()
        val fadeEnd = clipHeight - unit * inset
        val fadeStart = fadeEnd - unit * FadeUnits
        drawRect(Brush.verticalGradient(listOf(Color.Black, Color.Transparent), fadeStart, fadeEnd), blendMode = BlendMode.DstIn)
    }
}

private data class OpacityElement(val level: Opacity) : ModifierNodeElement<OpacityNode>() {
    override fun create() = OpacityNode(level)
    override fun update(node: OpacityNode) {
        node.level = level
        node.invalidateLayer()
    }
}

private class OpacityNode(var level: Opacity) : Modifier.Node(), LayoutModifierNode, CompositionLocalConsumerModifierNode {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val opacity = when (level) {
            Opacity.Low -> theme.opacity.low
            Opacity.High -> theme.opacity.high
        }
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) { alpha = opacity }
        }
    }
}

/** The length of the fade at a clip, in units: the web's `1rem`. */
private const val FadeUnits = 2

/** The web's `--moon-shadow`: two soft rings of black at 5%. */
private val moonShadows = listOf(
    Shadow(radius = 4.dp, color = Color.Black.copy(alpha = 0.05f), spread = 4.dp),
    Shadow(radius = 12.dp, color = Color.Black.copy(alpha = 0.05f), spread = 12.dp),
)
