package streetlight.web.model

import kampfire.model.reactIn
import kampfire.model.Tap
import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.html.AppRoute
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.model.ui.CityListRoute
import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.CityRoute
import streetlight.model.ui.GalaxyListRoute
import streetlight.model.ui.GalaxyMapRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.PostMapRoute
import streetlight.model.ui.ProfileConfigRoute
import streetlight.model.ui.StarConfigRoute
import streetlight.model.ui.StarDashRoute
import streetlight.model.ui.StarRoute

/**
 * The navigation dock of the current route: a title and main, left, and right route links.
 *
 * A page can add to the dock with [mergeState]; a merge for a route not yet current waits for that route.
 */
class RouteDock(scope: CoroutineScope, val portal: Portal) {
    private val state = storeOf<RouteDockState?>(null)
    private var stateRoute: AppRoute? = null
    private var pending: RouteDockMerge? = null
    private val visible = storeOf(true)

    val titleState = state.tapOf { it?.title }
    val mainRoutes = state.tapOf { it?.mainRoutes }
    val leftRoutes = state.tapOf { it?.leftRoutes }
    val rightRoutes = state.tapOf { it?.rightRoutes }
    val isVisibleState: Tap<Boolean> = visible

    init {
        portal.routeState.reactIn(scope) { route ->
            state.set { stateOf(route) }
            stateRoute = route
            visible.set(true)
            pending?.takeIf { it.route == route }?.let { applyMerge(it.state) }
            pending = null
        }
    }

    /** Sets the non-null parts of [merge] on the dock of [route]. */
    fun mergeState(route: AppRoute, merge: RouteDockState) {
        when (route) {
            stateRoute -> applyMerge(merge)
            else -> pending = RouteDockMerge(route, merge)
        }
    }

    fun setVisible(isVisible: Boolean) {
        visible.set(isVisible)
    }

    private fun applyMerge(merge: RouteDockState) {
        state.set {
            val base = this ?: RouteDockState()
            base.copy(
                mainRoutes = merge.mainRoutes ?: base.mainRoutes,
                title = merge.title ?: base.title,
                leftRoutes = merge.leftRoutes ?: base.leftRoutes,
                rightRoutes = merge.rightRoutes ?: base.rightRoutes,
            )
        }
    }
}

data class RouteDockState(
    val mainRoutes: List<AppRoute>? = null,
    val title: String? = null,
    val leftRoutes: List<AppRoute>? = null,
    val rightRoutes: List<AppRoute>? = null,
)

private data class RouteDockMerge(
    val route: AppRoute,
    val state: RouteDockState,
)

private fun stateOf(route: AppRoute): RouteDockState? {
    return when (route) {
        // universe
        is HomeRoute -> universeStateOf(PostMapRoute())
        is GalaxyListRoute -> universeStateOf(GalaxyMapRoute(null))
        is CityListRoute -> universeStateOf(CityMapRoute(null))

        // galaxy
        is GalaxyRoute -> RouteDockState(
            mainRoutes = listOf(route, GalaxyMapRoute(route.slug)),
            leftRoutes = listOf(HomeRoute),
        )

        // city
        is CityRoute -> RouteDockState(listOf(route, CityMapRoute(route.slug)))

        // star
        is StarRoute -> RouteDockState(listOf(route), title = route.username.value)
        is StarDashRoute, is ProfileConfigRoute, is StarConfigRoute -> RouteDockState(
            rightRoutes = listOf(ProfileConfigRoute),
        )

        // location
        is LocationRoute -> RouteDockState(listOf(route))

        // earth
        is PostMapRoute -> RouteDockState(
            mainRoutes = listOf(route, GalaxyMapRoute(null), CityMapRoute(null)),
            title = route.title,
            leftRoutes = listOf(HomeRoute),
        )
        is GalaxyMapRoute -> when (val slug = route.slug) {
            null -> RouteDockState(
                mainRoutes = listOf(PostMapRoute(), route, CityMapRoute(null)),
                title = "Streetlight",
                leftRoutes = listOf(HomeRoute),
            )
            else -> RouteDockState(
                mainRoutes = listOf(GalaxyRoute(slug), route),
                leftRoutes = listOf(GalaxyMapRoute(null)),
            )
        }
        is CityMapRoute -> when (val slug = route.slug) {
            null -> RouteDockState(
                mainRoutes = listOf(PostMapRoute(), GalaxyMapRoute(null), route),
                title = "Streetlight",
                leftRoutes = listOf(HomeRoute),
            )
            else -> RouteDockState(
                mainRoutes = listOf(CityRoute(slug), route),
                leftRoutes = listOf(CityMapRoute(null)),
            )
        }

        else -> null
    }
}

private fun universeStateOf(mapRoute: AppRoute) = RouteDockState(
    mainRoutes = listOf(HomeRoute, GalaxyListRoute, CityListRoute),
    title = "Streetlight",
    rightRoutes = listOf(mapRoute),
)
