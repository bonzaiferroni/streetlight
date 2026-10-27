package streetlight.compose.view

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/** The stage the parts shared between pages move across, provided by [AppPortal]. */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** The parts a page shares with the one that opens it. */
enum class SharedPart {
    /** The frame that grows into the other page's frame, its contents fading between. */
    Frame,

    /** The image, the same picture on both pages. */
    Image,

    /** The title, resized between its two styles. */
    Title,
}

/** The key of a [part] shared between pages, both of which name the same [route]. */
data class SharedKey(val route: Any, val part: SharedPart)

/**
 * Shares this [part] of the page of [route] with the other page that shows it, so it moves between them as the pages
 * change; an image as one element, a frame or a title as bounds whose content fades between. Without a route, or
 * outside the [AppPortal], it shares nothing.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPart(route: Any?, part: SharedPart): Modifier {
    val stage = LocalSharedTransitionScope.current ?: return this
    if (route == null) return this
    val pageScope = LocalNavAnimatedContentScope.current
    return with(stage) {
        val state = rememberSharedContentState(SharedKey(route, part))
        when (part) {
            SharedPart.Image -> this@sharedPart.sharedElement(state, pageScope)
            SharedPart.Frame, SharedPart.Title -> this@sharedPart.sharedBounds(state, pageScope)
        }
    }
}
