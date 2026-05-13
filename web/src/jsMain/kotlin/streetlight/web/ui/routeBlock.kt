package streetlight.web.ui

import koala.dom.RenderContext
import koala.dom.routeBlock
import koala.html.AppRoute
import koala.model.Portal
import org.w3c.dom.HTMLElement
import streetlight.web.StreetlightRoute
import streetlight.web.model.Streetlight

inline fun <reified Route: StreetlightRoute> RenderContext.routeBlock(
    renderCacheCount: Int? = null,
    crossinline block: RenderContext.(Route) -> Unit,
): HTMLElement = routeBlock<Route>(portal, renderCacheCount, block)

inline fun <reified Route: StreetlightRoute, Data> RenderContext.routeBlock(
    crossinline provideData: suspend (Route) -> Data?,
    renderCacheCount: Int? = null,
    crossinline block: RenderContext.(Data) -> Unit
): HTMLElement = routeBlock<Route, Data>(portal, provideData, renderCacheCount, block)