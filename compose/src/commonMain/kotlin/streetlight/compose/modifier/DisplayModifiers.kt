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
import streetlight.compose.view.drawLights

/**
 * Rounds the corners to [units] of the theme's unit and clips the content to them. The web's `BorderRadius(n)`.
 *
 * [moonShadow] casts the web's `MoonShadow` behind the content and [outline] draws the theme's low outline, the web's
 * `--outline-low`, inside the edge, both following the corners.
 */
fun Modifier.borderRadius(units: Number, moonShadow: Boolean = false, outline: Boolean = false) =
    this then BorderRadiusElement(units.toFloat(), moonShadow, outline)

/** Paints the theme's quiet background behind the content. The web's `ZenBg`. */
fun Modifier.zenBg() = this then BackgroundElement(Background.Zen)

/** Paints the theme's card background behind the content, a step firmer than [zenBg]. The web's `CardBg`. */
fun Modifier.cardBg() = this then BackgroundElement(Background.Card)

/**
 * Clips the content at [height] units and fades it over the two units that end [inset] units above the clip, as the
 * web's text clip with its mask of fixed stops: only content that reaches the clip fades.
 */
fun Modifier.fadeClip(height: Number, inset: Number = 0) = this then FadeClipElement(height.toFloat(), inset.toFloat())

/**
 * Paints paper behind the content with the page's lights shining on it, their positions and reach taken within its
 * bounds.
 */
fun Modifier.shineSurface() = this then ShineSurfaceElement

/** Draws the content at the theme's low opacity. The web's `OpacityLow`. */
fun Modifier.opacityLow() = this then OpacityElement(Opacity.Low)

/** Draws the content at the theme's high opacity, a step quieter than full. The web's `OpacityHigh`. */
fun Modifier.opacityHigh() = this then OpacityElement(Opacity.High)

/** The theme's opacity levels a modifier applies. */
private enum class Opacity { Low, High }

private data class BorderRadiusElement(
    val units: Float,
    val moonShadow: Boolean,
    val outline: Boolean,
) : ModifierNodeElement<BorderRadiusNode>() {
    override fun create() = BorderRadiusNode(units, moonShadow, outline)
    override fun update(node: BorderRadiusNode) {
        node.units = units
        node.moonShadow = moonShadow
        node.outline = outline
        node.painters = null
        node.invalidateLayer()
        node.invalidateDraw()
    }
}

private class BorderRadiusNode(var units: Float, var moonShadow: Boolean, var outline: Boolean) :
    Modifier.Node(), LayoutModifierNode, DrawModifierNode, CompositionLocalConsumerModifierNode {
    var painters: List<DropShadowPainter>? = null
    private var paintedUnit: Dp? = null

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

    // the shadow falls outside the clipped layer and the outline is drawn over it
    override fun ContentDrawScope.draw() {
        val unit = theme.unit
        if (moonShadow) {
            val shadows = painters?.takeIf { paintedUnit == unit } ?: moonShadows.map { shadow ->
                requireGraphicsContext().shadowContext.createDropShadowPainter(RoundedCornerShape(unit * units), shadow)
            }.also {
                painters = it
                paintedUnit = unit
            }
            shadows.forEach { with(it) { draw(size) } }
        }
        drawContent()
        if (outline) {
            val width = theme.line.toPx()
            val corner = (unit * units).toPx()
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

private data object ShineSurfaceElement : ModifierNodeElement<ShineSurfaceNode>() {
    override fun create() = ShineSurfaceNode()
    override fun update(node: ShineSurfaceNode) = Unit
}

private class ShineSurfaceNode : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
    override fun ContentDrawScope.draw() {
        drawRect(theme.colors.paper)
        drawLights(theme.lights)
        drawContent()
    }
}

private data class FadeClipElement(val height: Float, val inset: Float) : ModifierNodeElement<FadeClipNode>() {
    override fun create() = FadeClipNode(height, inset)
    override fun update(node: FadeClipNode) {
        node.height = height
        node.inset = inset
        node.invalidateMeasurement()
    }
}

private class FadeClipNode(var height: Float, var inset: Float) :
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
