package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.routeBlock
import org.w3c.dom.HTMLElement
import streetlight.model.ui.StreetlightRoute

// convenience functions so we don't need to pass portal as an arg, probably should nix
inline fun <reified Route: StreetlightRoute> ViewScope.routeBlock(
    crossinline block: ViewScope.(Route) -> Unit,
): HTMLElement = routeBlock<Route>(portal, block)

inline fun <reified Route: StreetlightRoute, Data> ViewScope.routeBlock(
    crossinline provideData: suspend (Route) -> Data?,
    refreshOnRoute: Boolean = true,
    crossinline block: ViewScope.(Data) -> Unit
): HTMLElement = routeBlock<Route, Data>(portal, provideData, refreshOnRoute, block)