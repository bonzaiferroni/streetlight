package streetlight.compose.modifier

// a surface lit by the page's lights, which has no web cousin

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import streetlight.compose.utility.rotateHue
import streetlight.compose.view.GlowFalloff
import streetlight.compose.view.GlowState
import streetlight.compose.view.KoalaLights
import streetlight.compose.view.glowRadius
import streetlight.compose.view.glowStops
import kotlin.math.abs

/**
 * Paints paper behind the content with the page's lights shining on it from around it: each pools on the edge its
 * direction from the page's center points to, as far as [reach] says.
 */
fun Modifier.shineSurface(reach: ShineReach = ShineReach.Surface) = this then ShineSurfaceElement(reach)

/** How far each light of a [shineSurface] reaches. */
enum class ShineReach {
    /** The light's radius on the page, a circle. */
    Glow,

    /** Four times the light's radius as a fraction of the surface's width and of its height, an ellipse. */
    Surface,
}

private data class ShineSurfaceElement(val reach: ShineReach) : ModifierNodeElement<ShineSurfaceNode>() {
    override fun create() = ShineSurfaceNode(reach)
    override fun update(node: ShineSurfaceNode) {
        node.reach = reach
        node.invalidateDraw()
    }
}

private class ShineSurfaceNode(var reach: ShineReach) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
    override fun ContentDrawScope.draw() {
        drawRect(theme.colors.paper)
        drawShine(theme.lights, reach)
        drawContent()
    }
}

/** Draws [lights] on a surface the size of the draw area, each reaching as far as [reach] says. */
private fun DrawScope.drawShine(lights: KoalaLights, reach: ShineReach) {
    val hue = lights.hue
    val pageSize = lights.pageSize
    lights.glows.forEach { glow ->
        when (reach) {
            ShineReach.Glow -> drawShineGlow(glow, hue, pageSize)
            ShineReach.Surface -> drawShineSurface(glow, hue)
        }
    }
}

/** Draws [glow] as a circle of its radius on a page of [pageSize], centered on the edge its direction points to. */
private fun DrawScope.drawShineGlow(glow: GlowState, hue: Float, pageSize: Size) {
    val radius = glowRadius(glow, pageSize)
    if (radius < 1f) return

    val color = glow.color.value.rotateHue(hue).let { it.copy(alpha = it.alpha * ShineIntensity) }
    val brush = Brush.radialGradient(*glowStops(color, glow.focus.value, ShineFalloff), center = anchorOf(glow), radius = radius)
    drawRect(brush)
}

/** Draws [glow] as an ellipse, its radius a fraction of the surface's sides, centered on the edge its direction points to. */
private fun DrawScope.drawShineSurface(glow: GlowState, hue: Float) {
    val radiusX = size.width * glow.radius.value * ShineSpread
    val radiusY = size.height * glow.radius.value * ShineSpread
    if (radiusX < 1f || radiusY < 1f) return

    val anchor = anchorOf(glow)
    val color = glow.color.value.rotateHue(hue).let { it.copy(alpha = it.alpha * ShineIntensity) }
    val brush = Brush.radialGradient(*glowStops(color, glow.focus.value, ShineFalloff), center = anchor, radius = radiusX)
    val scaleY = radiusY / radiusX
    // a circle squashed about the anchor into the ellipse, over a rect that covers the surface once squashed
    scale(1f, scaleY, pivot = anchor) {
        drawRect(brush, topLeft = Offset(0f, anchor.y - anchor.y / scaleY), size = Size(size.width, size.height / scaleY))
    }
}

/** The point on the surface's edge that [glow]'s direction from the page's center points to, or its center. */
private fun DrawScope.anchorOf(glow: GlowState): Offset {
    // the direction scaled by its max norm is a point on a square, stretched to the surface's edge
    val dx = glow.x.value - 0.5f
    val dy = glow.y.value - 0.5f
    val norm = maxOf(abs(dx), abs(dy))
    return if (norm == 0f) center else center + Offset(dx / norm * size.width / 2, dy / norm * size.height / 2)
}

/** The fade of a shine, gentler than the page's so its color holds further across a small surface. */
private val ShineFalloff = GlowFalloff(0.80f, 0.55f, 0.30f, 0.12f)

/** The multiple of a light's radius a [ShineReach.Surface] shine reaches, half of it falling beyond the edge. */
private const val ShineSpread = 3f

/** The fraction of a light's energy its shine casts. */
private const val ShineIntensity = 1.25f
