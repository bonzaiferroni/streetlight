package koala.dom

import koala.html.AppRoute
import koala.html.Id
import koala.model.FetcherContent
import koala.model.PortalState
import koala.model.RouteContent
import koala.model.RouteInflator
import koala.model.toContentOrNull
import kotlinx.browser.window

class RouteScope(
    override val viewDelegate: ViewScope,
    val inflator: RouteInflator,
    val state: PortalState,
): RebuildScope, ViewScope by viewDelegate

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

inline fun <reified Route: AppRoute, reified Data: FetcherContent> RouteScope.routeBlock(
    crossinline block: ViewScope.(Data) -> Unit
) = routeBlock<Route, Data>(
    provideData = { inflator.contentFor<Data>(it) },
    block = block
)

inline fun <reified Route: AppRoute, reified Data: FetcherContent> ViewScope.routeBlock(
    inflator: RouteInflator,
    state: PortalState,
    crossinline block: ViewScope.(Data) -> Unit
) {
    RouteScope(this, inflator, state).routeBlock<Route, Data>(block)
}