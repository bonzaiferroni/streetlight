package streetlight.web.model

import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.html.AppRoute
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.model.ui.GalaxyMapRoute
import streetlight.model.ui.GalaxyRoute

class RouteDock(scope: CoroutineScope, val portal: Portal) {
    private val state = storeOf<RouteDockState?>(null)
    val titleState = state.tapOf { it?.title }
    val mainRoutes = state.tapOf { it?.mainRoutes }
    val leftRoutes = state.tapOf { it?.leftRoutes }
    val rightRoutes = state.tapOf { it?.rightRoutes }

    init {
        portal.routeState.reactIn(scope) { route ->
            state.set { stateOf(route) }
        }
    }

    fun mergeState(merge: RouteDockState) {

    }
}

data class RouteDockState(
    val mainRoutes: List<AppRoute>? = null,
    val title: String? = null,
    val leftRoutes: List<AppRoute>? = null,
    val rightRoutes: List<AppRoute>? = null,
)

private fun stateOf(route: AppRoute): RouteDockState? {
    return when (route) {
        is GalaxyRoute -> RouteDockState(listOf(route, GalaxyMapRoute(route.slug)))
        else -> null
    }
}