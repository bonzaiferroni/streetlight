package koala.dom

import koala.dom.readIsland
import koala.html.AppRoute
import koala.html.Id
import koala.model.PortalState
import koala.model.RouteContent
import koala.model.RouteInflator
import kotlinx.browser.window
import kotlinx.html.div
import org.w3c.dom.HTMLElement

class RouteScope(
    override val viewDelegate: ViewScope,
    val inflator: RouteInflator,
    val state: PortalState,
): DelegatedViewScope, ViewScope by viewDelegate

inline fun <reified Route: AppRoute, Data> RouteScope.routeBlock(
    crossinline provideData: suspend (Route) -> Data?,
    crossinline block: ViewScope.(Data) -> Unit
): HTMLElement {
    val state = state
    val route = state.route as? Route ?: error("route was not the expected type: ${Route::class.simpleName}")
    val element = div()

    launchEffect {
        val data = provideData(route)
        view.mountChildView("routeBlock", element) {
            when (data) {
                null -> textBlock("Something went wrong.")
                else -> {
                    block(data)
                    if (!state.isInitialRoute) {
                        window.scrollTo(0.0, state.initialScrollY)
                    }
                }
            }
        }
    }

    return element
}

inline fun <reified Route: AppRoute, reified Data: RouteContent> RouteScope.routeBlock(
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