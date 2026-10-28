package streetlight.compose.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import streetlight.compose.model.MediaForgeRoute
import streetlight.compose.model.PostGalaxy
import streetlight.model.data.Galaxy

/**
 * The screen of [route]: its name as its top bar, and, while a star is signed in, the galaxy the post goes to once read,
 * if any, shown in the [MediaForgeView].
 */
@Composable
fun MediaForgeScreen(route: MediaForgeRoute) {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.TwoTone("Media", "Forge"), scrollState)
    StarGate {
        Inflated<PostGalaxy>(route) { target -> MediaForgeView(target.galaxy, scrollState) }
    }
}

/**
 * The view of [MediaForgeRoute], in a column scrolled by [scrollState] under the top bar: the name of the [galaxy] a media post is
 * posted to, if any, over the form that creates it. The cousin of the web's `viewMediaForge`.
 */
@Composable
fun MediaForgeView(galaxy: Galaxy?, scrollState: ScrollState) {
    ConfigBody(galaxy?.name, scrollState) {
        Placeholder("The post's content, theme and layout")
    }
}
