package streetlight.compose.view

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import streetlight.compose.utility.rotateHue
import kotlin.math.hypot

/**
 * The lights of the current [streetlight.model.data.PageTheme]: its [glows], their colors turned by the flux [hue], and
 * the [pageSize] they light.
 */
@Stable
class KoalaLights(val glows: List<GlowState>, private val hueState: State<Float>, private val pageSizeState: State<Size>) {
    val hue get() = hueState.value
    val pageSize get() = pageSizeState.value
}

/** The animated values of one glow, read while drawing. */
class GlowState(
    val color: State<Color>,
    val x: State<Float>,
    val y: State<Float>,
    val radius: State<Float>,
    val focus: State<Float>,
)

/** Draws [lights] over the draw area, their positions and radii taken as fractions of its size. */
fun DrawScope.drawLights(lights: KoalaLights) {
    val hue = lights.hue
    lights.glows.forEach { drawGlow(it, hue) }
}

/** Draws [glow] as a radial gradient, its color turned by [hue], with the stops of the web's glows. */
private fun DrawScope.drawGlow(glow: GlowState, hue: Float) {
    val radius = glowRadius(glow, size)
    if (radius < 1f) return

    val color = glow.color.value.rotateHue(hue)
    val brush = Brush.radialGradient(
        *glowStops(color, glow.focus.value),
        center = Offset(size.width * glow.x.value, size.height * glow.y.value),
        radius = radius,
    )
    drawRect(brush)
}

/** The radius of [glow] on a page of [pageSize]. */
internal fun glowRadius(glow: GlowState, pageSize: Size) = hypot(pageSize.width, pageSize.height) * glow.radius.value

/** The stops of the web's glows in [color], solid up to [focus] and fading to transparent from there. */
private fun glowStops(color: Color, focus: Float): Array<Pair<Float, Color>> {
    val solid = focus.coerceIn(0f, 0.99f)
    fun stop(fraction: Float, alpha: Float) = solid + (1 - solid) * fraction to color.copy(alpha = color.alpha * alpha)
    return arrayOf(
        solid to color,
        stop(0.30f, 0.55f),
        stop(0.55f, 0.28f),
        stop(0.75f, 0.12f),
        stop(0.90f, 0.04f),
        1f to Color.Transparent,
    )
}
