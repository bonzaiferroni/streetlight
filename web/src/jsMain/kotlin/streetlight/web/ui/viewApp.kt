package streetlight.web.ui

import kampfire.model.GeoPoint
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
import kotlinx.datetime.TimeZone
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.serializer
import revealContent
import streetlight.model.data.AreaTransitState
import streetlight.model.data.TransitVehicle
import streetlight.web.HomeRoute
import streetlight.web.StreetlightScreen
import streetlight.web.io.ApiClient
import streetlight.web.io.FetchClient
import streetlight.web.io.OSMFetchClient
import streetlight.web.io.TransitBrowserClient
import streetlight.web.model.Streetlight
import streetlight.web.model.ChatRoom
import streetlight.web.model.ClientContext
import streetlight.web.model.GateAgent
import streetlight.web.model.SiteConfig
import streetlight.web.model.StreetMap
import streetlight.web.model.UserCache
import streetlight.web.model.UserCred
import streetlight.web.model.UserGate
import streetlight.web.model.UserInterest
import streetlight.web.pages.AppBody
import streetlight.web.pages.emptyBadge

@OptIn(ExperimentalSerializationApi::class)
fun viewApp() {
    val scope = MainScope() // 57 KB

    val cred = UserCred()
    val fetchClient = FetchClient(cred)

    val app = object: Streetlight { // 220 KB
        override val appScope = scope

        override val config = SiteConfig()

        override val client = object: ClientContext {
            override val transit = TransitBrowserClient(fetchClient)
            override val api = ApiClient(fetchClient)
            override val location = OSMFetchClient()
        }

        override val gate = UserGate(scope, cred, client.api)
        override val userCache = UserCache(scope, client.api, gate)
        override val portal = Portal(HomeRoute(), StreetlightScreen.entries, scope)
        override val gateAgent = GateAgent(scope, gate, portal)

        override val geoMap = GeoMap(scope)
        override val streetMap = StreetMap(scope, client, userCache, geoMap, config)
        override val chatRoom = ChatRoom(scope, client.api)
        override val userInterest = UserInterest(scope, client.api)
    } as Streetlight

    scope.launch {
        app.gate.readUser()

        val shellBox = document.getElementById(AppBody.shellBoxId)
        shellBox.style.display = "none"

        val portalMount = document.getElementById(AppBody.portalMountId)
        portalMount.renderRoot(app.appScope) {
            flowBlock(
                flow = app.portal.screenFlow,
                modifiers = modify(Blur, SlideUp),
                renderCacheCount = 0, // allows unlimited caching
                magic = true,
                onTransition = { window.scrollTo(0.0, 0.0) },
            ) { screen ->
                viewOf(app) {
                    when (screen) {
                        StreetlightScreen.Home -> viewHome()
                        StreetlightScreen.Event -> viewEventRoute(app)
                        StreetlightScreen.Account -> viewAccount(app)
                        StreetlightScreen.EditEvent -> viewEventEditorRoute()
                        StreetlightScreen.Sandbox -> viewSandbox(app)
                        StreetlightScreen.FullMap -> viewFullMap(app)
                        StreetlightScreen.EditStory -> viewPostEditor(app)
                        StreetlightScreen.Chat -> viewChatRoom(app)
                        StreetlightScreen.SongProfile -> viewSongProfile(app)
                        StreetlightScreen.EditTalent -> editTalentForm(app)
                        StreetlightScreen.ReadEvent -> viewEventScoutRoute(app)
                        StreetlightScreen.LocationProfile -> locationProfileView(app)
                        StreetlightScreen.EditLocation -> viewEditLocationRoute(app)
                        StreetlightScreen.LocationAdmin -> viewLocationAdmin(app)
                        StreetlightScreen.ScoutMap -> viewLocationScout(app)
                        StreetlightScreen.FoundGalaxy -> viewGalaxyFoundry(app)
                        StreetlightScreen.GalaxyList -> viewGalaxyList(app)
                        StreetlightScreen.GalaxyProfile -> viewGalaxyProfileRoute()
                        StreetlightScreen.EventScout -> viewEventScoutRoute()
                        StreetlightScreen.EditProfile -> viewProfileEditor()
                        else -> textBlock("Coming soon: $screen")
                    }
                }
            }

            wireBlock(AppBody.titlePathId) {
                val titleFlow = app.portal.stateFlow.mapDistinct { it.title }
                flowBlock(titleFlow, defaultMagic, magic = true) { title ->
                    if (title != null) {
                        row {
                            heading2("|", modify(Dim))
                            heading2(title, modify(Dim))
                        }
                    }
                }
            }

            wireBlock(AppBody.badgeId) {
                flowBlock(app.gate.userFlow, defaultMagic, magic = true) { userInfo ->
                    val avatarUrl = userInfo?.avatarUrl
                    if (avatarUrl != null) {
                        image(avatarUrl, modify(Size100, Square, CircleClip))
                    } else {
                        box(modify(Height100, Square)) {
                            emptyBadge()
                        }
                    }
                }
            }
        }

        delay(100)
        revealContent()
    }
}

