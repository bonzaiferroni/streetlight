package streetlight.web.ui

import koala.dom.ScopedDOM
import koala.dom.routeBlock
import org.w3c.dom.HTMLElement
import streetlight.web.StreetlightRoute

// convenience functions so we don't need to pass portal as an arg, probably should nix
inline fun <reified Route: StreetlightRoute> ScopedDOM.routeBlock(
    renderCacheCount: Int? = null,
    crossinline block: ScopedDOM.(Route) -> Unit,
): HTMLElement = routeBlock<Route>(portal, renderCacheCount, block)

inline fun <reified Route: StreetlightRoute, Data> ScopedDOM.routeBlock(
    crossinline provideData: suspend (Route) -> Data?,
    renderCacheCount: Int? = null,
    refreshOnRoute: Boolean = true,
    crossinline block: ScopedDOM.(Data) -> Unit
): HTMLElement = routeBlock<Route, Data>(portal, provideData, renderCacheCount, refreshOnRoute, block)