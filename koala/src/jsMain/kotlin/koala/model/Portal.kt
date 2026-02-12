package koala.model

import koala.html.AppRoute
import koala.html.AppScreen
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class Portal(
    val initialRoute: AppRoute,
    val screens: List<AppScreen>,
    scope: CoroutineScope
): BrowserModel<PortalState>(PortalState(initialRoute), scope) {
    val screenFlow = stateFlow.mapDistinct { it.route.screen }
    val routeFlow = stateFlow.mapDistinct { it.route }

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

    inline fun <reified T> routeFlowOf(): Flow<T> {
        return routeFlow.mapDistinct {
            try {
                it as? T
            } catch(e: Exception) {
                console.log("narr!")
                console.log(e)
                throw(e)
            }
        }.filterNotNull()
    }

    inline fun <reified T> routeOrNullFlowOf(): Flow<T?> {
        return routeFlow.mapDistinct { it as? T }
    }

    fun go(route: AppRoute) {
        go(route, stateNow.backstack + stateNow.route)
    }

    fun goBack() {
        val route = stateNow.backstack.lastOrNull() ?: return
        go(route, stateNow.backstack.dropLast(1))
    }

    private fun go(route: AppRoute, backstack: List<AppRoute>) {
        setState { it.copy(route = route, backstack = backstack)}
        hashPath = route.toHashPath()
    }

    private fun routeOf(hashPath: String): AppRoute {
        return AppRoute.routeOf(hashPath, screens) { initialRoute }
    }
}

data class PortalState(
    val route: AppRoute,
    val backstack: List<AppRoute> = emptyList()
) {
    val canGoBack get() = backstack.isNotEmpty()
}

