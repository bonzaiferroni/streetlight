package streetlight.web.ui

import koala.dom.AppScope
import koala.dom.ViewContext
import koala.dom.readIsland
import koala.html.Id
import streetlight.web.model.Streetlight

@Deprecated("Use RenderContext")
typealias AppContextProto = ViewContext<Streetlight>

val AppContextProto.api get() = model.client.api
val AppContextProto.portal get() = model.portal
val AppContextProto.userCache get() = model.cache
val AppContextProto.toaster get() = model.toaster

inline fun <reified T> AppScope.readIsland(
    elementId: Id,
    checkId: (T) -> Boolean,
): T? = when (portal.stateNow.isInitialRoute) {
    true -> readIsland<T?>(elementId)?.takeIf { checkId(it) }
    else -> null
}

