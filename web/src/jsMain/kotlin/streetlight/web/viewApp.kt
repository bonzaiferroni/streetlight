@file:Suppress("UnsafeCastFromDynamic")
@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web

import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalJsExport::class)
@JsExport
fun viewApp() {
    console.log("loading streetlight")

    val scope = MainScope() // 57 KB

    val app = object: AppContext { // 220 KB
        override val appScope = scope

        override val client = object: ClientContext {
            override val gtfs = GtfsBrowserClient()
            override val event = EventBrowserClient()
        }

        override val portal = AppPortal(scope)
        override val gate = UserGate(scope)
        override val gateAgent = GateAgent(scope, gate, portal)

        override val home by lazy {
            object: HomeContext {
                override val gtfsMap = GtfsMap(scope, client.gtfs)
                override val eventMap = EventMap(scope, client.event)
            }
        }
    }

    val portalMount = document.getElementById("portal-mount") as HTMLElement
    portalMount.renderRoot(app.appScope, app) {
        renderState(
            flow = portal.screenFlow,
            cacheRenderedElements = true
        ) { screen ->
            when (screen) {
                AppScreen.Home -> viewHome()
                AppScreen.Event -> viewEvent()
                AppScreen.Account -> viewAccount()
                AppScreen.CreateLocation -> viewCreateLocation()
                AppScreen.CreateEvent -> viewCreateEvent()
            }
        }
    }
}