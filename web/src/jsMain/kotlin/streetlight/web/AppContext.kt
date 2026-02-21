package streetlight.web

import koala.model.GeoMap
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope

interface AppContext {
    val appScope: CoroutineScope
    val client: ClientContext
    val portal: Portal
    val gate: UserGate
    val gateAgent: GateAgent
    val geoMap: GeoMap
    val streetMap: StreetMap
    val chatRoom: ChatRoom
    val userCache: UserCache
}

interface ClientContext {
    val transit: TransitBrowserClient
    val api: ApiClient
    val location: OSMClient
}