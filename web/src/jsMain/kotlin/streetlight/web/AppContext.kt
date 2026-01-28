package streetlight.web

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import org.w3c.dom.HTMLElement

interface AppContext {
    val appScope: CoroutineScope
    val client: ClientContext
    val portal: AppPortal
    val home: Home
}

interface Home {
    val gtfsMap: GtfsMap
    val eventMap: EventMap
}

interface ClientContext {
    val gtfs: GtfsBrowserClient
    val event: EventBrowserClient
}

fun AppContext.mountRender(
    elementId: String,
    block: RenderContext.() -> Unit
) {
    val mount = document.getElementById(elementId) as HTMLElement
    mount.renderRoot(appScope, this, block)
}