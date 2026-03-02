package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.heading2
import koala.model.GeoMap
import koala.model.Portal
import koala.model.mapDistinct
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import revealContent
import streetlight.web.pages.AppBody

fun viewApp() {
    val scope = MainScope() // 57 KB

    val cred = UserCred()
    val fetchClient = FetchClient(cred)

    val app = object: AppContext { // 220 KB
        override val appScope = scope

        override val client = object: ClientContext {
            override val transit = TransitBrowserClient(fetchClient)
            override val api = ApiClient(fetchClient)
            override val location = OSMFetchClient()
        }

        override val userCache = UserCache(scope, client.api)
        override val portal = Portal(HomeRoute(), StreetlightScreen.entries, scope)
        override val gate = UserGate(scope, cred, client.api, userCache)
        override val gateAgent = GateAgent(scope, gate, portal)

        override val geoMap = GeoMap(scope)
        override val streetMap = StreetMap(scope, client, geoMap)
        override val chatRoom = ChatRoom(scope, client.api)
    }

    scope.launch {
        app.gate.readUser()

        val shellBox = document.getElementById(AppBody.shellBoxId)
        shellBox.style.display = "none"

        val portalMount = document.getElementById(AppBody.portalMountId)
        portalMount.renderRoot(app.appScope) {
            flowBlock(
                flow = app.portal.screenFlow,
                modifiers = modify(Blur, SlideY),
                cacheRenderedElements = true,
                magic = true,
                onTransition = { window.scrollTo(0.0, 0.0) },
            ) { screen ->
                when (screen) {
                    StreetlightScreen.Home -> viewHome(app)
                    StreetlightScreen.Event -> viewEventRoute(app)
                    StreetlightScreen.Account -> viewAccount(app)
                    StreetlightScreen.EditEvent -> eventEditorRouteView(app)
                    StreetlightScreen.Sandbox -> viewSandbox(app)
                    StreetlightScreen.FullMap -> viewFullMap(app)
                    StreetlightScreen.EditStory -> viewPostEditor(app)
                    StreetlightScreen.Chat -> viewChatRoom(app)
                    StreetlightScreen.SongProfile -> viewSongProfile(app)
                    StreetlightScreen.EditTalent -> editTalentForm(app)
                    StreetlightScreen.ReadEvent -> eventReaderView(app)
                    StreetlightScreen.LocationProfile -> locationProfileView(app)
                    StreetlightScreen.EditLocation -> locationEditorRouteView(app)
                    StreetlightScreen.LocationAdmin -> viewLocationAdmin(app)
                    else -> textBlock("Coming soon: $screen")
                }
            }

            wireBlock(AppBody.titlePathId) {
                val titleFlow = app.portal.stateFlow.mapDistinct { it.title }
                flowBlock(titleFlow, modify(Blur, SlideX), magic = true) { title ->
                    if (title != null) {
                        row {
                            heading2("|", modify(Dim))
                            heading2(title, modify(Dim))
                        }
                    }
                }
            }
        }

        delay(100)
        revealContent()
    }
}