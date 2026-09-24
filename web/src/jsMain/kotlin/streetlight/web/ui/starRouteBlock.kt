package streetlight.web.ui

import koala.modifier.*
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.routeBlock
import koala.html.AppRoute
import koala.model.FetcherContent
import streetlight.model.data.Star

/** A `routeBlock` behind a [starGate]. */
inline fun <reified Route: AppRoute, reified Content: FetcherContent> RouteScope.starRouteBlock(
    mod: Modifier? = null,
    crossinline content: ViewScope.(Star, Content) -> Unit
) {
    starGate(mod) { star ->
        routeBlock<Route, Content>(this@starRouteBlock.inflator, this@starRouteBlock.state) { content ->
            content(star, content)
        }
    }
}