package streetlight.web.ui

import kampfire.api.Username
import kampfire.model.storeOf
import koala.css.ModifierSet
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.routeBlock
import koala.html.AppRoute
import koala.model.FetcherContent
import streetlight.model.data.InboxContent
import streetlight.model.data.Star
import streetlight.model.ui.InboxRoute

inline fun <reified Route: AppRoute, reified Content: FetcherContent> RouteScope.starRouteBlock(
    mod: ModifierSet? = null,
    crossinline content: ViewScope.(Star, Content) -> Unit
) {
    starGate(mod) { star ->
        routeBlock<Route, Content>(this@starRouteBlock.inflator, this@starRouteBlock.state) { content ->
            content(star, content)
        }
    }
}