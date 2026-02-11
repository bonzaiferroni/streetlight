package streetlight.web

import kotlinx.coroutines.CoroutineScope

interface AppContext {
    val appScope: CoroutineScope
    val client: ClientContext
    val portal: AppPortal
    val gate: UserGate
    val gateAgent: GateAgent
    val home: HomeContext
}

interface HomeContext {
    val geoMap: GeoMap
    val streetMap: StreetMap
    val eventCreator: EventCreator
}

interface ClientContext {
    val transit: TransitBrowserClient
    val api: ApiClient
    val location: OSMClient
}