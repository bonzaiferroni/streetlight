package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import koala.SvgFile
import streetlight.compose.utility.mixSrgb

/**
 * The Streetlight logo: the wordmark, "street" in the accent color and "light" in the primary, with its flame glowing
 * through the page's lights, over the moon's drop shadow. The cousin of the web's `textLogo`.
 */
@Composable
fun TextLogo(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val street = rememberSvgPainter(SvgFile.LogoTextStreet)
    val light = rememberSvgPainter(SvgFile.LogoTextLight)
    val flame = rememberSvgPainter(SvgFile.LogoFlame)
    val time = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { time.floatValue = (it - start) / 1_000_000_000f }
    }
    // the page's lights in the web's order: rho, beta, gamma
    val lightColors = { theme.lights.glows.map { it.color.value.copy(alpha = 1f) } }

    Box(modifier.aspectRatio(LogoRatio)) {
        // the web's MoonDropShadow: the whole logo in paper, lowered and blurred
        Spacer(
            Modifier.matchParentSize()
                .offset(y = 4.dp)
                .blur(4.dp, BlurredEdgeTreatment.Unbounded)
                .drawBehind { listOf(street, light, flame).forEach { drawMask(it, theme.colors.paper) } }
        )
        Spacer(
            Modifier.matchParentSize().drawBehind {
                drawMask(street, theme.colors.accentFg)
                drawMask(light, theme.colors.primaryFg)
            }
        )
        // the web's GlowShadow: the flame's shadow drifting through the lights
        Spacer(
            Modifier.matchParentSize()
                .graphicsLayer {
                    val frame = GlowShadow.at(time.floatValue)
                    translationX = frame.dx.dp.toPx()
                    translationY = frame.dy.dp.toPx()
                    renderEffect = BlurEffect(frame.blur.dp.toPx(), frame.blur.dp.toPx(), TileMode.Decal)
                }
                .drawBehind { drawMask(flame, GlowShadow.colorAt(time.floatValue, lightColors())) }
        )
        // the web's GlowBackground: the flame itself cycling through the lights
        Spacer(
            Modifier.matchParentSize().drawBehind {
                drawMask(flame, GlowBackground.colorAt(time.floatValue, lightColors()))
            }
        )
    }
}

/** Draws [painter] over the whole area in [color], as the web's mask does. */
private fun DrawScope.drawMask(painter: Painter, color: Color) {
    with(painter) { draw(size, colorFilter = ColorFilter.tint(color)) }
}

/** A keyframe of a glow at [at] of its cycle: the light it shows, by its index, and its drop shadow's offset and blur. */
private class GlowFrame(val at: Float, val light: Int, val dx: Float = 0f, val dy: Float = 0f, val blur: Float = 0f)

/** A glow animation of [frames] over [seconds], as the web's `@keyframes`, played linearly and on repeat. */
private class GlowAnimation(val seconds: Float, val frames: List<GlowFrame>) {
    /** The frames around [time] and how far between them it is. */
    private fun spanAt(time: Float): Triple<GlowFrame, GlowFrame, Float> {
        val progress = time % seconds / seconds
        val next = frames.indexOfFirst { it.at >= progress }.coerceAtLeast(1)
        val from = frames[next - 1]
        val to = frames[next]
        return Triple(from, to, (progress - from.at) / (to.at - from.at))
    }

    /** The color at [time], mixed between the [lights] of its frames. */
    fun colorAt(time: Float, lights: List<Color>): Color {
        val (from, to, fraction) = spanAt(time)
        return lights[to.light].mixSrgb(lights[from.light], fraction)
    }

    /** The drop shadow's offset and blur at [time]. */
    fun at(time: Float): GlowFrame {
        val (from, to, fraction) = spanAt(time)
        fun lerp(a: Float, b: Float) = a + (b - a) * fraction
        return GlowFrame(fraction, 0, lerp(from.dx, to.dx), lerp(from.dy, to.dy), lerp(from.blur, to.blur))
    }
}

/** The web's `glow-background` keyframes over 10 seconds. */
private val GlowBackground = GlowAnimation(
    10f,
    listOf(GlowFrame(0f, 0), GlowFrame(.25f, 1), GlowFrame(.4f, 0), GlowFrame(.65f, 2), GlowFrame(.8f, 1), GlowFrame(1f, 0)),
)

/** The web's `glow-shadow` keyframes over 9 seconds, each offset and blur in dp for the web's px. */
private val GlowShadow = GlowAnimation(
    9f,
    listOf(
        GlowFrame(0f, 0, blur = 2f),
        GlowFrame(.25f, 1, dy = -1f, blur = 3f),
        GlowFrame(.4f, 0, dx = 1f, blur = 1f),
        GlowFrame(.65f, 2, dy = -1f, blur = 2f),
        GlowFrame(.8f, 1, dx = -1f, blur = 1f),
        GlowFrame(1f, 0, blur = 2f),
    ),
)

/** The logo's width over its height, the web's `aspect-ratio: 4 / 1`. */
private const val LogoRatio = 4f
