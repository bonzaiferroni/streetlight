package streetlight.web

import koala.css.MagicBlur
import koala.css.SlideY
import koala.css.modify
import koala.dom.renderRoot
import koala.dom.flowBlock
import koala.dom.getElementById
import koala.model.GeoMap
import koala.model.Portal
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import streetlight.web.pages.AppBody

fun viewApp() {
    val scope = MainScope() // 57 KB

    val app = object: AppContext { // 220 KB
        val context = this

        override val appScope = scope

        override val client = object: ClientContext {
            override val transit = TransitBrowserClient(context)
            override val api = ApiClient(context)
            override val location = OSMClient(context)
        }

        override val portal = Portal(HomeRoute(), StreetlightScreen.entries, scope)
        override val gate = UserGate(context)
        override val gateAgent = GateAgent(scope, gate, portal)

        override val geoMap = GeoMap(scope)
        override val streetMap = StreetMap(scope, client, geoMap)
        override val eventEditor = EventEditor(scope, client, geoMap)
        override val eventProfile = EventProfile(scope, client)
        override val postEditor = PostEditor(scope, client, geoMap)
        override val chatRoom = ChatRoom(scope, client.api)

        override val userHub by lazy { UserHub(scope, client.api) }
    }

    val shellBox = document.getElementById(AppBody.shellBoxId)
    shellBox.style.display = "none"

    val portalMount = document.getElementById(AppBody.portalMountId)
    portalMount.renderRoot(app.appScope) {
        flowBlock(
            flow = app.portal.screenFlow,
            modifiers = modify(MagicBlur, SlideY),
            cacheRenderedElements = true,
            animate = true,
        ) { screen ->
            when (screen) {
                StreetlightScreen.Home -> viewHome(app)
                StreetlightScreen.Event -> viewEventRoute(app)
                StreetlightScreen.Account -> viewAccount(app)
                StreetlightScreen.EditEvent -> viewEventEditor(app)
                StreetlightScreen.Sandbox -> viewSandbox(app)
                StreetlightScreen.FullMap -> viewFullMap(app)
                StreetlightScreen.EditStory -> viewPostEditor(app)
                StreetlightScreen.Chat -> viewChatRoom(app)
            }
        }
    }
}