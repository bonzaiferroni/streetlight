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
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import streetlight.compose.modifier.FiligreeStyle
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
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

    /** The view's [text] in a filigree, as a map's title. */
    data class Title(val text: String) : TopBar

    /** A title in the logo's two colors, [accent] then [primary], in a filigree, as the web's `configBody` heading. */
    data class TwoTone(val accent: String, val primary: String) : TopBar
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
 * [TopBarUnits] of room above its content for the bar. A bar known from the route is mounted by its screen, before the
 * content is read, so a change of page between two views with the same bar leaves it in place. The mount leaves as its
 * page starts to leave, so the bar fades out with the page.
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
    val height = theme.unit * TopBarUnits
    val heightPx = with(LocalDensity.current) { height.toPx() }

    // a mount with the same bar as the last takes its place unchanged; each bar fades by its own mount's scroll, so one
    // that leaves fades out where it stood
    AnimatedContent(
        host.active,
        modifier.fillMaxWidth().height(height),
        transitionSpec = { fadeIn(theme.magic.eased()) togetherWith fadeOut(theme.magic.eased()) },
        contentKey = { it?.bar },
    ) { mount ->
        mount ?: return@AnimatedContent
        val fade = { 1f - (mount.scrolled() / heightPx).coerceIn(0f, 1f) }
        val isGone by remember(mount, heightPx) { derivedStateOf { fade() == 0f } }
        if (isGone) return@AnimatedContent
        val bar = mount.bar
        Box(
            Modifier.fillMaxWidth()
                .height(height)
                .graphicsLayer { alpha = fade() }
                .hazeBlur(HazeInput.Sources(LocalPageHaze.current), TopBarBlur)
                .background(theme.colors.paper.copy(alpha = GlassAlpha))
        ) {
            when (bar) {
                TopBar.Logo -> AppHeader()
                is TopBar.Title -> TopBarTitle(bar.text)
                is TopBar.TwoTone -> TopBarTwoTone(bar.accent, bar.primary)
            }
        }
    }
}

/** The [text] of a view in a filigree, its rays stopping a unit short of the helm bar's icons, as the [AppHeader]'s. */
@Composable
private fun TopBarTitle(text: String) {
    val theme = currentTheme
    BasicText(
        text,
        Modifier.fillMaxSize().padding(horizontal = RayInsetUnits).filigree().wrapContentHeight(),
        style = theme.text.heading3.copy(lineHeight = 1.em, textAlign = TextAlign.Center),
        color = { theme.colors.ink },
    )
}

/** Shows [bar] as the top bar of a view whose grid scrolls by [gridState], fading as its content scrolls under it. */
@Composable
fun TopBarMount(bar: TopBar, gridState: LazyStaggeredGridState) =
    TopBarMount(bar, remember(gridState) { gridState.scrolledUnder() })

/** Shows [bar] as the top bar of a view whose column scrolls by [scrollState], fading as its content scrolls under it. */
@Composable
fun TopBarMount(bar: TopBar, scrollState: ScrollState) =
    TopBarMount(bar, remember(scrollState) { scrollState.scrolledUnder() })

/** Shows [bar] as the top bar of a view that does not scroll, so the bar stays whole. */
@Composable
fun TopBarMount(bar: TopBar) = TopBarMount(bar, Unscrolled)

/** The pixels this grid has scrolled under a top bar, all of them once its first item has left. */
private fun LazyStaggeredGridState.scrolledUnder(): () -> Float = {
    if (firstVisibleItemIndex > 0) Float.MAX_VALUE else firstVisibleItemScrollOffset.toFloat()
}

/** The pixels this scroll has moved its content under a top bar. */
private fun ScrollState.scrolledUnder(): () -> Float = { value.toFloat() }

/** The scroll of a view that does not scroll. */
private val Unscrolled = { 0f }

/**
 * The title of [accent] in the accent's foreground followed by [primary] in the primary's, between the accent and
 * primary rays, as the logo is.
 */
@Composable
private fun TopBarTwoTone(accent: String, primary: String) {
    val theme = currentTheme
    val style = theme.text.heading3.copy(lineHeight = 1.em)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(Modifier.fillMaxWidth().padding(horizontal = RayInsetUnits).filigree(FiligreeStyle.Ray)) {
            BasicText(accent, style = style, color = { theme.colors.accentFg })
            BasicText(primary, style = style, color = { theme.colors.primaryFg })
        }
    }
}

/** The height of a top bar in units: the helm bar's icon and a unit above and below it. */
const val TopBarUnits = 2 + HelmIconUnits

/** The blur of the page beneath the bar, the route dock's `--strong-blur` of 10px. */
private val TopBarBlur = HazeBlurStyle { blurRadius(10.dp) }

/** The glass of the bar, the route dock's paper at `.6`. */
private const val GlassAlpha = 0.6f
