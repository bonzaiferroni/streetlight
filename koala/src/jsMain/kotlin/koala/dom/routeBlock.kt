package koala.dom

import koala.css.*
import koala.html.AppRoute
import koala.model.Portal
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement

inline fun <reified Route: AppRoute> ViewScope.routeBlock(
    portal: Portal,
    crossinline block: ViewScope.(Route) -> Unit
): HTMLElement {
    val routeFlow = portal.routeFlowOf<Route>()

    val element = flowBlock(routeFlow) {
        block(it)
    }

    return element
}

inline fun <reified Route: AppRoute, Data> ViewScope.routeBlock(
    portal: Portal,
    crossinline provideData: suspend (Route) -> Data?,
    refreshOnRoute: Boolean = true,
    crossinline block: ViewScope.(Data) -> Unit
): HTMLElement {
    val routeFlow = portal.routeFlowOf<Route>(!refreshOnRoute).map { provideData(it) }

    val element = flowBlock(routeFlow, modify(Width100P, Magic)) {
        if (it != null) {
            block(it)
        } else {
            textBlock("Something went wrong.")
        }
    }

    return element
}