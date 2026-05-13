package streetlight.web.model

import koala.model.GeoMap
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.web.HomeRoute
import streetlight.web.StreetlightScreen
import streetlight.web.io.ApiClient
import streetlight.web.io.FetchClient
import streetlight.web.io.OSMClient
import streetlight.web.io.OmniLog
import streetlight.web.io.TransitClient

interface Streetlight {
    val appScope: CoroutineScope
    val client: ClientFacade
    val portal: Portal
    val gate: UserGate
    val gateAgent: GateAgent
    val geoMap: GeoMap
    val streetMap: StreetMap
    val chatRoom: ChatRoom
    val cache: DataCache
    val config: SiteConfig
    val omni: OmniLog
    val stage: StageFacade
    val toaster: Toaster
}

interface ClientFacade {
    val transit: TransitClient
    val api: ApiClient
    val location: OSMClient
}

interface StageFacade {
    val galaxy: GalaxyStage
}

fun createStreetlight(scope: CoroutineScope): Streetlight {

    val cred = CredentialStore()
    val fetchClient = FetchClient(cred)

    return object : Streetlight { // 220 KB
        override val appScope = scope

        override val config = SiteConfig()

        override val client = object: ClientFacade {
            override val transit = TransitClient(fetchClient)
            override val api = ApiClient(fetchClient)
            override val location = OSMClient()
        }

        override val gate = UserGate(scope, cred, client.api)
        override val cache = DataCache(scope, config, client.api, gate)
        override val portal = Portal(HomeRoute, StreetlightScreen.entries, scope)
        override val gateAgent = GateAgent(scope, gate, portal)

        override val geoMap = GeoMap(scope)
        override val streetMap = StreetMap(scope, cache, geoMap)
        override val chatRoom = ChatRoom(scope, client.api)
        override val omni = OmniLog(scope, client.api)

        override val stage = object: StageFacade {
            override val galaxy = GalaxyStage(scope)
        }

        override val toaster = Toaster(scope)
    } as Streetlight
}