package streetlight.web.ui

import koala.dom.DOMRender
import koala.dom.routeBlock
import org.w3c.dom.HTMLElement
import streetlight.web.StreetlightRoute

// convenience functions so we don't need to pass portal as an arg, probably should nix
inline fun <reified Route: StreetlightRoute> DOMRender.routeBlock(
    renderCacheCount: Int? = null,
    crossinline block: DOMRender.(Route) -> Unit,
): HTMLElement = routeBlock<Route>(portal, renderCacheCount, block)

inline fun <reified Route: StreetlightRoute, Data> DOMRender.routeBlock(
    crossinline provideData: suspend (Route) -> Data?,
    renderCacheCount: Int? = null,
    refreshOnRoute: Boolean = true,
    crossinline block: DOMRender.(Data) -> Unit
): HTMLElement = routeBlock<Route, Data>(portal, provideData, renderCacheCount, refreshOnRoute, block)