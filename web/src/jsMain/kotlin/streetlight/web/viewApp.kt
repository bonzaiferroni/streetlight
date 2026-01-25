@file:Suppress("UnsafeCastFromDynamic")
@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalJsExport::class)
@JsExport
fun viewApp() {
    val scope = MainScope() // 57 KB
    val navigator = AppNavigator("/", scope) // 2 KB

    val app = object: AppContext { // 220 KB
        override val client = object: ClientContext {
            override val gtfs = GtfsBrowserClient()
            override val event = EventBrowserClient()
        }

        override val navigator = navigator

        override val home by lazy {
            object: Home {
                override val gtfsMap = GtfsMap(scope, client.gtfs)
                override val eventMap = EventMap(scope, client.event)
            }
        }
    }

    val navigatorElement = window.document.getElementById("app-navigator") as HTMLElement
    navigatorElement.renderRoot(scope, app) {
//        renderState(navigator.stateFlow.mapDistinct { it.screen }) { screen ->
//            when(screen) {
//                BrowserScreen.Home -> viewHome()
//                BrowserScreen.Event -> TODO()
//                BrowserScreen.User -> TODO()
//            }
//        }
        viewHome()
    }
}