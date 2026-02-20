package koala.dom

import koala.html.AppRoute
import koala.model.Portal
import kotlinx.coroutines.flow.map

inline fun <reified Route: AppRoute> RenderContext.routeBlock(
    portal: Portal,
    crossinline block: RenderContext.(Route) -> Unit
) {
    val routeFlow = portal.routeFlowOf<Route>()

    flowBlock(routeFlow) {
        block(it)
    }
}

inline fun <reified Route: AppRoute, Data> RenderContext.routeBlock(
    portal: Portal,
    crossinline provideData: suspend (Route) -> Data?,
    crossinline block: RenderContext.(Data) -> Unit
) {
    val routeFlow = portal.routeFlowOf<Route>().map { provideData(it) }

    flowBlock(routeFlow) {
        if (it != null) {
            block(it)
        } else {
            textBlock("Content not found")
        }
    }
}