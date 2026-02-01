@file:Suppress("UnsafeCastFromDynamic")
@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web

import koala.dom.renderRoot
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalJsExport::class)
@JsExport
fun viewApp() {
    console.log("loading streetlight")

    val scope = MainScope() // 57 KB

    val app = object: AppContext { // 220 KB
        val context = this

        override val appScope = scope

        override val client = object: ClientContext {
            override val gtfs = GtfsBrowserClient(context)
            override val event = EventBrowserClient(context)
            override val location = LocationBrowserClient()
        }

        override val portal = AppPortal(scope)
        override val gate = UserGate(context)
        override val gateAgent = GateAgent(scope, gate, portal)

        override val home by lazy {
            object: HomeContext {
                override val gtfsMap = GtfsMap(scope, client.gtfs)
                override val eventMap = EventMap(context)
            }
        }
    }

    val portalMount = document.getElementById("portal-mount") as HTMLElement
    portalMount.renderRoot(app.appScope) {
        renderState(
            flow = app.portal.screenFlow,
            cacheRenderedElements = true
        ) { screen ->
            when (screen) {
                AppScreen.Home -> viewHome(app)
                AppScreen.Event -> viewEvent(app.portal)
                AppScreen.Account -> viewAccount(app.gate, app.portal)
                AppScreen.CreateLocation -> viewCreateLocation(app.home.eventMap, app.portal)
                AppScreen.CreateEvent -> viewCreateEvent(app.portal, app.home.eventMap)
            }
        }
    }

//    scope.launch {
//        val msg = app.client.event.readSecure()
//        console.log(msg)
//    }
}