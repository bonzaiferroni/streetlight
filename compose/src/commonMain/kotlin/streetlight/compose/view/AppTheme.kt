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
import androidx.compose.foundation.layout.Box
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
import kotlin.math.hypot

/** Shows [content] over the background lights of the current theme, and provides its [AppColors]. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val theme by koinInject<ThemeHost>().themeState.collectAsState()

    val accentState = animateColorAsState(theme.accent.toColor(), themeTransition())
    val primaryState = animateColorAsState(theme.primary.toColor(), themeTransition())
    val paper = Koala.paper.toColor()
    val colors = remember(accentState, primaryState) {
        AppColors(paper, Koala.ink.toColor(), accentState, primaryState)
    }

    val isLit = theme.backgroundMode != PageBackgroundMode.None
    val rho = animateGlow(theme.rho, isLit)
    val beta = animateGlow(theme.beta, isLit)
    val gamma = animateGlow(theme.gamma, isLit)
    val hueState = when (theme.backgroundMode) {
        PageBackgroundMode.Flux -> animateFlux()
        else -> remember { mutableFloatStateOf(0f) }
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        Box(Modifier.fillMaxSize().background(paper)) {
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
