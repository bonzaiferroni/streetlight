package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.model.GeoMap
import koala.model.Portal
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.w3c.dom.HTMLImageElement
import revealContent
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
import streetlight.web.model.ThemeReactor
import streetlight.web.model.UserCache
import streetlight.web.model.UserCred
import streetlight.web.model.UserGate
import streetlight.web.pages.AppBodyKey
import streetlight.web.pages.StarBadgeKey
import streetlight.web.pages.starBadge

@OptIn(ExperimentalSerializationApi::class)
fun viewApp() {
    val scope = MainScope() // 57 KB

    val cred = UserCred()
    val fetchClient = FetchClient(cred)

    val app = object : Streetlight { // 220 KB
        override val appScope = scope

        override val config = SiteConfig()

        override val client = object : ClientContext {
            override val transit = TransitBrowserClient(fetchClient)
            override val api = ApiClient(fetchClient)
            override val location = OSMFetchClient()
        }

        override val gate = UserGate(scope, cred, client.api)
        override val cache = UserCache(scope, config, client.api, gate)
        override val portal = Portal(HomeRoute, StreetlightScreen.entries, scope)
        override val gateAgent = GateAgent(scope, gate, portal)

        override val geoMap = GeoMap(scope)
        override val streetMap = StreetMap(scope, client, cache, geoMap, config)
        override val chatRoom = ChatRoom(scope, client.api)
    } as Streetlight

    ThemeReactor(scope, app.config)

    scope.launch {
        app.gate.readUser()

        val shellBox = document.getElementById(AppBodyKey.ShellBoxId)
        shellBox.style.display = "none" // td: use pointer-events: none

        val portalMount = document.getElementById(AppBodyKey.PortalMountId)

        portalMount.renderRoot(app.appScope) {
            flowBlock(
                flow = app.portal.screenFlow,
                modifiers = modify(Magic, Blur, SlideUp),
                renderCacheCount = 0, // allows unlimited caching
                onTransition = { window.scrollTo(0.0, 0.0) },
            ) { screen ->
                viewContextOf(app) {
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
                        StreetlightScreen.LocationProfile -> locationProfileView(app)
                        StreetlightScreen.EditLocation -> viewEditLocationRoute(app)
                        StreetlightScreen.LocationAdmin -> viewLocationAdmin(app)
                        StreetlightScreen.ScoutMap -> viewLocationScoutOld(app)
                        StreetlightScreen.CreateGalaxy -> viewGalaxyFoundry()
                        StreetlightScreen.GalaxyList -> viewGalaxyList(app)
                        StreetlightScreen.GalaxyProfile -> viewGalaxyProfileRoute()
                        StreetlightScreen.EventScout -> viewEventScoutRoute()
                        StreetlightScreen.LocationScout -> viewLocationScoutRoute()
                        StreetlightScreen.EditProfile -> viewProfileEditor()
                        StreetlightScreen.SiteConfig -> viewSiteConfig()
                        else -> textBlock("Coming soon: $screen")
                    }
                }
            }

            wireBadges(app)
        }

        // When content is loaded with a hash tag like https://streetlight.ing/#/g/my-galaxy,
        // the home page content will be initially loaded. The opacity is initially 0 to avoid confusion, this
        // reveals the intended content when the script has rendered it.
        delay(100)
        revealContent()
    }
}

