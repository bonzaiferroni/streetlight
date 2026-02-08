package streetlight.web

import koala.html.AppRoute
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope

class AppPortal(
    scope: CoroutineScope
): BrowserModel<AppNavigatorState>(AppNavigatorState(), scope) {
    val screenFlow = stateFlow.mapDistinct { it.route.screen }

    var hashPath
        get() = window.location.hash.split("?")[0]
        set(value: String) {
            val query = window.location.hash.split("?").getOrNull(1)
            window.location.hash = value + (query?.let { "?$it" } ?: "")
        }

    init {
        val route = routeOf(hashPath)
        go(route)
        window.addEventListener("hashchange", {
            val route = routeOf(hashPath)
            if (route.screen == stateNow.route.screen) return@addEventListener
            go(route)
        })
    }

    fun go(route: StreetlightRoute) {
        go(route, stateNow.backstack + stateNow.route)
    }

    fun goBack() {
        val route = stateNow.backstack.lastOrNull() ?: return
        go(route, stateNow.backstack.dropLast(1))
    }

    private fun go(route: StreetlightRoute, backstack: List<StreetlightRoute>) {
        setState { it.copy(route = route, backstack = backstack)}
        hashPath = route.toHashPath()
    }

    private fun routeOf(hashPath: String): StreetlightRoute {
        return AppRoute.routeOf(hashPath, StreetlightScreen.entries) { HomeRoute() }
    }
}

data class AppNavigatorState(
    val route: StreetlightRoute = HomeRoute(),
    val backstack: List<StreetlightRoute> = emptyList()
) {
    val canGoBack get() = backstack.isNotEmpty()
}

