package streetlight.web

import koala.dom.RenderContext
import koala.dom.renderRoot
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import org.w3c.dom.HTMLElement

interface AppContext {
    val appScope: CoroutineScope
    val client: ClientContext
    val portal: AppPortal
    val gate: UserGate
    val gateAgent: GateAgent
    val home: HomeContext
}

interface HomeContext {
    val gtfsMap: GtfsMap
    val eventMap: EventMap
}

interface ClientContext {
    val gtfs: GtfsBrowserClient
    val event: EventBrowserClient
    val location: LocationBrowserClient
}

fun AppContext.mountRender(
    elementId: String,
    block: RenderContext.() -> Unit
) {
    val mount = document.getElementById(elementId) as HTMLElement
    mount.renderRoot(appScope, block)
}