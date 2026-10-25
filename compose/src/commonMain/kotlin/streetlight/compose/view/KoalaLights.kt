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

/**
 * The stops of a glow in [color], solid up to [focus] and fading to transparent from there, its alpha at 30%, 55%, 75%
 * and 90% of the fade given by [falloff], the web's glows by default.
 */
internal fun glowStops(color: Color, focus: Float, falloff: GlowFalloff = PageFalloff): Array<Pair<Float, Color>> {
    val solid = focus.coerceIn(0f, 0.99f)
    fun stop(fraction: Float, alpha: Float) = solid + (1 - solid) * fraction to color.copy(alpha = color.alpha * alpha)
    return arrayOf(
        solid to color,
        stop(0.30f, falloff.at30),
        stop(0.55f, falloff.at55),
        stop(0.75f, falloff.at75),
        stop(0.90f, falloff.at90),
        1f to Color.Transparent,
    )
}

/** The alphas of a glow's fade at its fixed stops, as fractions of its color's alpha. */
internal class GlowFalloff(val at30: Float, val at55: Float, val at75: Float, val at90: Float)

/** The fade of the web's glows. */
private val PageFalloff = GlowFalloff(0.55f, 0.28f, 0.12f, 0.04f)
