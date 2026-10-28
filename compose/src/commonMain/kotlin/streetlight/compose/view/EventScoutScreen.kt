package streetlight.compose.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import streetlight.compose.model.EventScoutRoute
import streetlight.compose.model.PostGalaxy
import streetlight.model.data.Galaxy

/**
 * The screen of [route]: its name as its top bar, and, while a star is signed in, the galaxy the post goes to once read,
 * if any, shown in the [EventScoutView].
 */
@Composable
fun EventScoutScreen(route: EventScoutRoute) {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.TwoTone("Event", "Scout"), scrollState)
    StarGate {
        Inflated<PostGalaxy>(route) { target -> EventScoutView(target.galaxy, scrollState) }
    }
}

/**
 * The view of [EventScoutRoute], in a column scrolled by [scrollState] under the top bar: the name of the [galaxy] an event is
 * posted to, if any, over the form that creates it. The cousin of the web's `viewEventScout`.
 */
@Composable
fun EventScoutView(galaxy: Galaxy?, scrollState: ScrollState) {
    ConfigBody(galaxy?.name, scrollState) {
        Placeholder("The event's location, then the event itself")
    }
}
