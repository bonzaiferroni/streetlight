package streetlight.web

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope

class AppPortal(
    scope: CoroutineScope
): BrowserModel<AppNavigatorState>(AppNavigatorState(), scope) {
    val screenFlow = stateFlow.mapDistinct { it.route.screen }

    init {
        window.addEventListener("hashchange", {
            console.log(window.location.hash)
            val route = StreetlightRoute.fromHashFragment(window.location.hash)
            if (route.screen == stateNow.route.screen) return@addEventListener
            go(route)
        })
    }

    fun go(route: StreetlightRoute) {
        setState { it.copy(route = route) }
        window.location.hash = route.toHashPath()
    }
}

data class AppNavigatorState(
    val route: StreetlightRoute = Home()
)

fun StreetlightRoute.Companion.fromHashFragment(fragment: String): StreetlightRoute {
    val fragment = fragment.dropStart('#').dropStart('/')
    val querySplit = fragment.split('?')
    val path = querySplit[0].lowercase().split('/')
    return when (path[0]) {
        Home.screen.path -> Home()
        Account.screen.path -> Account
        EventRoute.screen.path -> EventRoute
        else -> Home()
    }
}

private fun String.dropStart(char: Char) = if (startsWith(char)) drop(1) else this