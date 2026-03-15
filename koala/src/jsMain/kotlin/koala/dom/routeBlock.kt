package koala.dom

import koala.css.*
import koala.html.AppRoute
import koala.model.Portal
import koala.utils.prettyPrint
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement

inline fun <reified Route: AppRoute> RenderContext.routeBlock(
    portal: Portal,
    renderCacheCount: Int? = null,
    crossinline block: RenderContext.(Route) -> Unit
): HTMLElement {
    val routeFlow = portal.routeFlowOf<Route>()

    val element = flowBlock(routeFlow, renderCacheCount = renderCacheCount) {
        block(it)
    }

    return element
}

inline fun <reified Route: AppRoute, Data> RenderContext.routeBlock(
    portal: Portal,
    crossinline provideData: suspend (Route) -> Data?,
    renderCacheCount: Int? = null,
    crossinline block: RenderContext.(Data) -> Unit
): HTMLElement {
    // td: retry provideData call n times
    val routeFlow = portal.routeFlowOf<Route>().map { provideData(it) }

    val element = flowBlock(routeFlow, modify(Width100), renderCacheCount = renderCacheCount) {
        if (it != null) {
            block(it)
        } else {
            textBlock("Content not found")
        }
    }

    return element
}