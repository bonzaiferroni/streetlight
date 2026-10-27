package streetlight.compose.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.draw.drawBehind
import koala.modifier.Glow
import koala.modifier.Koala
import koala.modifier.KoalaTheme
import org.koin.compose.koinInject
import streetlight.compose.model.ThemeHost
import streetlight.compose.utility.collectAsState
import streetlight.compose.utility.toColor
import streetlight.model.data.PageBackgroundMode
import streetlight.model.data.PageTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth

/**
 * Shows [content] over the background lights of the current [PageTheme], and provides the [KoalaAppTheme] read as
 * [currentTheme], sized from a rem chosen by the window's width, below a platform's [bar], such as the desktop window's
 * title bar.
 */
@Composable
fun AppTheme(bar: @Composable () -> Unit = {}, content: @Composable () -> Unit) {
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
    val pageSize = remember { mutableStateOf(Size.Zero) }
    val lights = remember(hueState) { KoalaLights(listOf(rho, beta, gamma), hueState, pageSize) }

    BoxWithConstraints(Modifier.fillMaxSize().onSizeChanged { pageSize.value = it.toSize() }) {
        val rem = if (maxWidth < NarrowMaxWidth) NarrowRem else WideRem
        val text = remember(family, rem) { KoalaTypography(family, rem) }
        val koalaAppTheme = remember(accentState, primaryState, text, lights) {
            val colors = KoalaColors(paper, Koala.ink.toColor(), Opacity, accentState, primaryState)
            KoalaAppTheme(unitOf(rem), text, colors, Opacity, lights, Magic)
        }

        val pageHaze = rememberHazeState()
        CompositionLocalProvider(
            LocalKoalaAppTheme provides koalaAppTheme,
            LocalPageHaze provides pageHaze,
            LocalSchemeColor provides koalaAppTheme.colors.ink,
        ) {
            // the page, paper and lights and all, is what a floating card blurs behind itself; the paper keeps the capture
            // opaque, so the blur covers what lies behind the card
            Column(Modifier.fillMaxSize()) {
                bar()
                Box(Modifier.fillMaxWidth().weight(1f).hazeSource(pageHaze).background(paper)) {
                    Spacer(
                        Modifier.fillMaxSize()
                            .graphicsLayer()
                            .drawBehind { drawLights(lights) }
                    )
                    content()
                }
            }
        }
    }
}

/** The rem of a window narrower than [NarrowMaxWidth], the web's `87.5%` below 600px. */
private val Opacity = KoalaOpacity()
private val Magic = KoalaMagic()

private val NarrowRem = 14.sp
private val WideRem = 16.sp
private val NarrowMaxWidth = 600.dp

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

private fun <T> themeTransition(): AnimationSpec<T> = tween(
    durationMillis = (KoalaTheme.ThemeTransitionSeconds * 1000).toInt(),
    easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f),
)

private const val FluxMillis = 30_000
