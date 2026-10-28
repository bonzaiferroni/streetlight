package streetlight.compose.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur

/** The content of a view's top bar, which the [TopBarHost] draws in a look shared by every view. */
sealed interface TopBar {
    /** The [AppHeader]: the logo between its rays. */
    data object Logo : TopBar
}

/** A view's [bar] and the pixels its content has [scrolled] under it, read while drawing. */
class TopBarMountState(bar: TopBar, scrolled: () -> Float) {
    var bar by mutableStateOf(bar)
    var scrolled by mutableStateOf(scrolled)
}

/** The mounts of the views that show a top bar, the latest the one shown. */
class TopBarHostState {
    val mounts = mutableStateListOf<TopBarMountState>()
    val active get() = mounts.lastOrNull()
}

/** The [TopBarHostState] of the app, shared by the views' mounts and the host in the overlay. */
val LocalTopBarHost = staticCompositionLocalOf<TopBarHostState> { error("No TopBarHost provided") }

/**
 * Shows [bar] as this view's top bar, fading as the view's content scrolls [scrolled] pixels under it. The view keeps
 * [TopBarUnits] of room above its content for the bar. The mount leaves as its page starts to leave, so the bar fades
 * out with the page.
 */
@Composable
fun TopBarMount(bar: TopBar, scrolled: () -> Float) {
    val host = LocalTopBarHost.current
    val mount = remember { TopBarMountState(bar, scrolled) }
    mount.bar = bar
    mount.scrolled = scrolled
    val isLeaving = LocalNavAnimatedContentScope.current.transition.targetState == EnterExitState.PostExit
    DisposableEffect(mount, isLeaving) {
        if (!isLeaving) host.mounts.add(mount)
        onDispose { host.mounts.remove(mount) }
    }
}

/**
 * Draws the top bar of the latest view with a [TopBarMount] across the top of the overlay, on the route dock's glass
 * that blurs the page. The bar fades as the view's content scrolls under it, gone once the content has scrolled its
 * height, when it stops taking clicks. A change of bar between views crossfades; a view with no bar leaves none.
 */
@Composable
fun TopBarHost(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val host = LocalTopBarHost.current
    val active = host.active
    val height = theme.unit * TopBarUnits
    val heightPx = with(LocalDensity.current) { height.toPx() }
    val fade = { active?.let { 1f - (it.scrolled() / heightPx).coerceIn(0f, 1f) } ?: 0f }
    val isGone by remember(active, heightPx) { derivedStateOf { fade() == 0f } }

    AnimatedContent(
        active?.bar,
        modifier.fillMaxWidth().height(height).graphicsLayer { alpha = fade() },
        transitionSpec = { fadeIn(theme.magic.eased()) togetherWith fadeOut(theme.magic.eased()) },
    ) { bar ->
        if (bar == null || isGone) return@AnimatedContent
        Box(
            Modifier.fillMaxWidth()
                .height(height)
                .hazeBlur(HazeInput.Sources(LocalPageHaze.current), TopBarBlur)
                .background(theme.colors.paper.copy(alpha = GlassAlpha))
        ) {
            when (bar) {
                TopBar.Logo -> AppHeader()
            }
        }
    }
}

/** The height of a top bar in units: the helm bar's icon and a unit above and below it. */
const val TopBarUnits = 2 + HelmIconUnits

/** The blur of the page beneath the bar, the route dock's `--strong-blur` of 10px. */
private val TopBarBlur = HazeBlurStyle { blurRadius(10.dp) }

/** The glass of the bar, the route dock's paper at `.6`. */
private const val GlassAlpha = 0.6f
