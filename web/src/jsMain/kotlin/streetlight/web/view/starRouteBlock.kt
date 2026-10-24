package streetlight.web.view

import koala.modifier.*
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.routeBlock
import koala.model.WebRoute
import koala.model.FetcherContent
import streetlight.model.data.Star

/** A `routeBlock` behind a [starGate]. */
inline fun <reified Route: WebRoute, reified Content: FetcherContent> RouteScope.starRouteBlock(
    mod: Modifier? = null,
    crossinline content: context(Star, Route) ViewScope.(Content) -> Unit
) {
    starGate(mod) {
        routeBlock<Route, Content>(this@starRouteBlock.inflator, this@starRouteBlock.state) {
            content(it)
        }
    }
}