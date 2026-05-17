package streetlight.web.ui

import koala.dom.AppContext
import koala.dom.RenderContext
import koala.dom.ViewContext
import koala.dom.readIsland
import koala.html.Id
import kotlinx.coroutines.CoroutineScope
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import streetlight.web.model.Streetlight

@Deprecated("Use RenderContext")
typealias AppContextProto = ViewContext<Streetlight>

val AppContextProto.api get() = model.client.api
val AppContextProto.portal get() = model.portal
val AppContextProto.userCache get() = model.cache
val AppContextProto.toaster get() = model.toaster

suspend inline fun <reified T> RenderContext.readIslandOrApi(
    id: Id,
    checkId: (T) -> Boolean,
    block: suspend () -> T?
): T? = when (portal.stateNow.isInitialRoute) {
    true -> readIsland<T?>(id)?.takeIf { checkId(it) } ?: block()
    else -> block()
}
