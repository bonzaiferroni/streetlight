package streetlight.web.ui

import koala.dom.AppScope
import koala.dom.routeBlock
import org.w3c.dom.HTMLElement
import streetlight.web.StreetlightRoute

// convenience functions so we don't need to pass portal as an arg, probably should nix
inline fun <reified Route: StreetlightRoute> AppScope.routeBlock(
    renderCacheCount: Int? = null,
    crossinline block: AppScope.(Route) -> Unit,
): HTMLElement = routeBlock<Route>(portal, renderCacheCount, block)

inline fun <reified Route: StreetlightRoute, Data> AppScope.routeBlock(
    crossinline provideData: suspend (Route) -> Data?,
    renderCacheCount: Int? = null,
    refreshOnRoute: Boolean = true,
    crossinline block: AppScope.(Data) -> Unit
): HTMLElement = routeBlock<Route, Data>(portal, provideData, renderCacheCount, refreshOnRoute, block)