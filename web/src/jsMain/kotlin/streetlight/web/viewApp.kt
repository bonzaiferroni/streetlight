@file:Suppress("UnsafeCastFromDynamic")
@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web

import koala.css.Blur
import koala.css.SlideY
import koala.css.modify
import koala.dom.renderRoot
import koala.dom.flowBlock
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
            override val transit = TransitBrowserClient(context)
            override val event = EventBrowserClient(context)
            override val location = LocationBrowserClient(context)
        }

        override val portal = AppPortal(scope)
        override val gate = UserGate(context)
        override val gateAgent = GateAgent(scope, gate, portal)

        override val home = object: HomeContext {
            override val geoMap = GeoMap(scope)
            override val streetMap = StreetMap(scope, client, geoMap)
            override val eventCreator = EventCreator(scope, client, streetMap)
        }
    }

    val portalMount = document.getElementById("portal-mount") as HTMLElement
    portalMount.renderRoot(app.appScope) {
        flowBlock(
            flow = app.portal.screenFlow,
            modifiers = modify(Blur, SlideY),
            cacheRenderedElements = true,
            animate = true,
        ) { screen ->
            when (screen) {
                AppScreen.Home -> viewHome(app)
                AppScreen.Event -> viewEvent(app.portal)
                AppScreen.Account -> viewAccount(app.gate, app.portal)
            }
        }
    }
}