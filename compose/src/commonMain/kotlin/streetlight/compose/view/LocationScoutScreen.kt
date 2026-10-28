package streetlight.compose.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import streetlight.compose.model.LocationScoutRoute
import streetlight.compose.model.PostGalaxy
import streetlight.model.data.Galaxy

/**
 * The screen of [route]: its name as its top bar, and, while a star is signed in, the galaxy the post goes to once read,
 * if any, shown in the [LocationScoutView].
 */
@Composable
fun LocationScoutScreen(route: LocationScoutRoute) {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.TwoTone("Location ", "Scout"), scrollState)
    StarGate {
        Inflated<PostGalaxy>(route) { target -> LocationScoutView(target.galaxy, scrollState) }
    }
}

/**
 * The view of [LocationScoutRoute], in a column scrolled by [scrollState] under the top bar: the name of the [galaxy] a location is
 * posted to, if any, over the form that creates it. The cousin of the web's `viewLocationScout`.
 */
@Composable
fun LocationScoutView(galaxy: Galaxy?, scrollState: ScrollState) {
    ConfigBody(galaxy?.name, scrollState) {
        Placeholder("The location's search and details")
    }
}
