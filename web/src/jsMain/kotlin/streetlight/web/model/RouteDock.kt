package streetlight.web.model

import kampfire.api.Slug
import kampfire.model.reactIn
import kampfire.model.Tap
import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.SvgFile
import koala.SvgPack
import koala.model.WebRoute
import koala.model.LabeledRoute
import koala.model.withLabel
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.model.ui.EarthRoute
import streetlight.model.ui.EventsMapRoute
import streetlight.model.ui.FeedRoute
import streetlight.model.ui.GalaxyMapRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.HomeRoute

/**
 * The navigation dock, read from the route: the [StableDock], or the [FocusDock] of the current route while it is
 * shown. A new focus shows its own dock first. The stable route marked is the last of the stable dock's routes
 * visited, as a tab bar marks its tab.
 */
class RouteDock(scope: CoroutineScope, val portal: Portal) {
    private val state = storeOf(RouteDockState())
    private val visible = storeOf(true)

    val focusDockState = state.tapOf { it.focusDock }
    val isFocusShownState = state.tapOf { it.isFocusShown }
    val shownDockState = state.tapOf { it.focusDock?.routes?.takeIf { _ -> it.isFocusShown } ?: StableDock }
    val stableRouteState = state.tapOf { it.stableRoute }
    val isVisibleState: Tap<Boolean> = visible

    init {
        portal.routeState.reactIn(scope) { portalRoute ->
            val route = portalRoute.dockRoute
            val focusDock = route.toFocusDock()
            state.set {
                copy(
                    focusDock = focusDock,
                    isFocusShown = isFocusShown || focusDock != this.focusDock,
                    stableRoute = route.toStableRoute() ?: stableRoute,
                )
            }
            visible.set(true)
        }
    }

    /** Switches between the focus dock and the [StableDock]. */
    fun toggleFocus() = state.set { copy(isFocusShown = !isFocusShown) }

    fun setVisible(isVisible: Boolean) {
        visible.set(isVisible)
    }
}

/** The focus dock of a [RouteDock], whether it is shown, and the stable route last visited. */
data class RouteDockState(
    val focusDock: FocusDock? = null,
    val isFocusShown: Boolean = true,
    val stableRoute: WebRoute = StableDock.first().route,
)

/** The routes of the dock shown on every page. */
val StableDock = listOf(
    HomeRoute().withLabel("Home"),
    EventsMapRoute().withLabel("Earth"),
)

/** The dock of the content a route focuses on: its [routes], and the [icon] that calls it up. */
data class FocusDock(val routes: List<LabeledRoute>, val icon: SvgPack)

/** The dock of the content this route focuses on, `null` for a route with no focus. */
private fun WebRoute.toFocusDock(): FocusDock? = when (this) {
    is GalaxyRoute -> galaxyDockOf(slug)
    is GalaxyMapRoute -> slug?.let { galaxyDockOf(it) }
    else -> null
}

private fun galaxyDockOf(slug: Slug) = FocusDock(
    routes = listOf(
        GalaxyRoute(slug).withLabel("Feed"),
        GalaxyMapRoute(slug).withLabel("Map"),
    ),
    icon = SvgFile.Planet,
)

/** The route of the [StableDock] this route lies under, `null` for one under none. */
private fun WebRoute.toStableRoute(): WebRoute? = when {
    this is HomeRoute -> StableDock[0].route
    this is GalaxyMapRoute && slug != null -> null
    this is EarthRoute -> StableDock[1].route
    else -> null
}

/** This route as the dock knows it: a feed route showing its default feed, so it is one route whichever feed it shows. */
internal val WebRoute.dockRoute get() = (this as? FeedRoute)?.defaultFeedRoute ?: this
