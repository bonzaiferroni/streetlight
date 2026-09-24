package koala.dom

import koala.html.AppRoute
import koala.html.Id
import koala.model.FetcherContent
import koala.model.PortalState
import koala.model.RouteContent
import koala.model.RouteInflator
import koala.model.toContentOrNull
import kotlinx.browser.window

/** The receiver of a route's view, holding the route's [state] and the [inflator] that fetches its content. */
class RouteScope(
    override val viewDelegate: ViewScope,
    val inflator: RouteInflator,
    val state: PortalState,
): RebuildScope, ViewScope by viewDelegate

/**
 * Builds the route's view with [block] from the content [provideData] supplies for the route.
 *
 * The content replaces what the view held. A route other than the initial one is scrolled to its remembered
 * position.
 */
inline fun <reified Route: AppRoute, Data: FetcherContent> RouteScope.routeBlock(
    crossinline provideData: suspend (Route) -> Data,
    crossinline block: ViewScope.(Data) -> Unit
) {
    val state = state
    val route = state.route as? Route ?: error("route was not the expected type: ${Route::class.simpleName}")

    launchEffect(RouteScope::class) {
        val data = provideData(route)
        this@routeBlock.rebuildContent {
            block(data)
            if (!state.isInitialRoute) {
                window.scrollTo(0.0, state.initialScrollY)
            }
        }
    }
}

/**
 * Builds the route's view with [block], reading the content from the shell's data island with [shellId] on the
 * initial route and fetching it otherwise.
 */
inline fun <reified Route: AppRoute, reified Data: FetcherContent> RouteScope.routeBlock(
    shellId: Id,
    crossinline block: ViewScope.(Data) -> Unit
) = routeBlock<Route, Data>(
    provideData = { route ->
        if (state.isInitialRoute) {
            readIsland<Data>(shellId) ?: error("no shell found: $shellId")
        } else {
            inflator.contentFor<Data>(route)
        }
    },
    block = block
)

/** Builds the route's view with [block] from content fetched for the route. */
inline fun <reified Route: AppRoute, reified Data: FetcherContent> RouteScope.routeBlock(
    crossinline block: ViewScope.(Data) -> Unit
) = routeBlock<Route, Data>(
    provideData = { inflator.contentFor<Data>(it) },
    block = block
)

/** Builds a view for the route in [state] with [block], from content fetched by [inflator]. */
inline fun <reified Route: AppRoute, reified Data: FetcherContent> ViewScope.routeBlock(
    inflator: RouteInflator,
    state: PortalState,
    crossinline block: ViewScope.(Data) -> Unit
) {
    RouteScope(this, inflator, state).routeBlock<Route, Data>(block)
}