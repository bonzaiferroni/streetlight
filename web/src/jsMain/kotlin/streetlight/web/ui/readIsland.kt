package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.readIsland
import koala.html.Id

/** The data the server rendered into [elementId], when this is the page's first route and [checkId] accepts it. */
inline fun <reified T> ViewScope.readIsland(
    elementId: Id,
    checkId: (T) -> Boolean,
): T? = when (portal.stateNow.isInitialRoute) {
    true -> readIsland<T?>(elementId)?.takeIf { checkId(it) }
    else -> null
}

