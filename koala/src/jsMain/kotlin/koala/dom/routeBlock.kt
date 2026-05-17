package koala.dom

import koala.css.*
import koala.html.AppRoute
import koala.model.Portal
import koala.model.mapDistinct
import koala.utils.prettyPrint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    refreshOnRoute: Boolean = true,
    crossinline block: RenderContext.(Data) -> Unit
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