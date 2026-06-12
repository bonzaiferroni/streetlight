package koala.dom

import koala.css.*
import koala.html.AppRoute
import koala.model.Portal
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement

inline fun <reified Route: AppRoute> RenderScope.routeBlock(
    portal: Portal,
    renderCacheCount: Int? = null,
    crossinline block: RenderScope.(Route) -> Unit
): HTMLElement {
    val routeFlow = portal.routeFlowOf<Route>()

    val element = flowBlock(routeFlow, renderCacheCount = renderCacheCount) {
        block(it)
    }

    return element
}

inline fun <reified Route: AppRoute, Data> RenderScope.routeBlock(
    portal: Portal,
    crossinline provideData: suspend (Route) -> Data?,
    renderCacheCount: Int? = null,
    refreshOnRoute: Boolean = true,
    crossinline block: RenderScope.(Data) -> Unit
): HTMLElement {
    val routeFlow = portal.routeFlowOf<Route>(!refreshOnRoute).map { provideData(it) }

    val element = flowBlock(routeFlow, modify(Width100P, Magic), renderCacheCount = renderCacheCount) {
        if (it != null) {
            block(it)
        } else {
            textBlock("Something went wrong.")
        }
    }

    return element
}