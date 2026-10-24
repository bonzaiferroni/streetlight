package streetlight.compose.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.State
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import koala.modifier.Glow
import koala.modifier.Koala
import koala.modifier.KoalaTheme
import org.koin.compose.koinInject
import streetlight.compose.model.ThemeHost
import streetlight.compose.utility.collectAsState
import streetlight.compose.utility.rotateHue
import streetlight.compose.utility.toColor
import streetlight.model.data.PageBackgroundMode
import streetlight.model.data.PageTheme
import kotlin.math.hypot

/**
 * Shows [content] over the background lights of the current [PageTheme], and provides the [KoalaAppTheme] read as
 * [currentTheme], sized from a rem chosen by the window's width.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val pageTheme by koinInject<ThemeHost>().themeState.collectAsState()

    val accentState = animateColorAsState(pageTheme.accent.toColor(), themeTransition())
    val primaryState = animateColorAsState(pageTheme.primary.toColor(), themeTransition())
    val paper = Koala.paper.toColor()
    val family = firaSans()

    val isLit = pageTheme.backgroundMode != PageBackgroundMode.None
    val rho = animateGlow(pageTheme.rho, isLit)
    val beta = animateGlow(pageTheme.beta, isLit)
    val gamma = animateGlow(pageTheme.gamma, isLit)
    val hueState = when (pageTheme.backgroundMode) {
        PageBackgroundMode.Flux -> animateFlux()
        else -> remember { mutableFloatStateOf(0f) }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(paper)) {
        val rem = if (maxWidth < NarrowMaxWidth) NarrowRem else WideRem
        val text = remember(family, rem) { KoalaTypography(family, rem) }
        val koalaAppTheme = remember(accentState, primaryState, text) {
            KoalaAppTheme(unitOf(rem), text, paper, Koala.ink.toColor(), accentState, primaryState)
        }

        CompositionLocalProvider(LocalKoalaAppTheme provides koalaAppTheme) {
            Spacer(
                Modifier.fillMaxSize()
                    .graphicsLayer()
                    .drawBehind {
                        val hue = hueState.value
                        drawGlow(rho, hue)
                        drawGlow(beta, hue)
                        drawGlow(gamma, hue)
                    }
            )
            content()
        }
    }
}

/** The rem of a window narrower than [NarrowMaxWidth], the web's `87.5%` below 600px. */
private val NarrowRem = 14.sp
private val WideRem = 16.sp
private val NarrowMaxWidth = 600.dp

/** The animated values of one background glow, read while drawing. */
private class GlowState(
    val color: State<Color>,
    val x: State<Float>,
    val y: State<Float>,
    val radius: State<Float>,
    val focus: State<Float>,
)

@Composable
private fun animateGlow(glow: Glow, isLit: Boolean) = GlowState(
    color = animateColorAsState(glow.toColor(), themeTransition()),
    x = animateFloatAsState(glow.position.x / 100f, themeTransition()),
    y = animateFloatAsState(glow.position.y / 100f, themeTransition()),
    radius = animateFloatAsState(if (isLit) glow.position.radius / 100f else 0f, themeTransition()),
    focus = animateFloatAsState(glow.focus / 100f, themeTransition()),
)

/** The hue angle of the color flux, turning once every 30 seconds. */
@Composable
private fun animateFlux() = rememberInfiniteTransition().animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(tween(FluxMillis, easing = LinearEasing)),
)

/** Draws [glow] as a radial gradient, its color turned by [hue], with the stops of the web's glows. */
private fun DrawScope.drawGlow(glow: GlowState, hue: Float) {
    val radius = hypot(size.width, size.height) * glow.radius.value
    if (radius < 1f) return

    val color = glow.color.value.rotateHue(hue)
    val focus = glow.focus.value.coerceIn(0f, 0.99f)
    fun stop(fraction: Float, alpha: Float) = focus + (1 - focus) * fraction to color.copy(alpha = color.alpha * alpha)
    val brush = Brush.radialGradient(
        focus to color,
        stop(0.30f, 0.55f),
        stop(0.55f, 0.28f),
        stop(0.75f, 0.12f),
        stop(0.90f, 0.04f),
        1f to Color.Transparent,
        center = Offset(size.width * glow.x.value, size.height * glow.y.value),
        radius = radius,
    )
    drawRect(brush)
}

private fun <T> themeTransition(): AnimationSpec<T> = tween(
    durationMillis = (KoalaTheme.ThemeTransitionSeconds * 1000).toInt(),
    easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f),
)

private const val FluxMillis = 30_000
