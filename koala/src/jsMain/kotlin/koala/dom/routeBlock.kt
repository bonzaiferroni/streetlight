package koala.dom

import koala.css.*
import koala.html.AppRoute
import koala.model.Portal
import koala.utils.prettyPrint
import kotlinx.coroutines.flow.map

inline fun <reified Route: AppRoute> RenderContext.routeBlock(
    portal: Portal,
    renderCacheCount: Int? = null,
    crossinline block: RenderContext.(Route) -> Unit
) {
    val routeFlow = portal.routeFlowOf<Route>()

    flowBlock(routeFlow, renderCacheCount = renderCacheCount) {
        block(it)
    }
}

inline fun <reified Route: AppRoute, Data> RenderContext.routeBlock(
    portal: Portal,
    crossinline provideData: suspend (Route) -> Data?,
    renderCacheCount: Int? = null,
    crossinline block: RenderContext.(Data) -> Unit
) {
    val routeFlow = portal.routeFlowOf<Route>().map { provideData(it) }

    flowBlock(routeFlow, modify(Width100), renderCacheCount = renderCacheCount) {
        if (it != null) {
            block(it)
        } else {
            textBlock("Content not found")
        }
    }
}