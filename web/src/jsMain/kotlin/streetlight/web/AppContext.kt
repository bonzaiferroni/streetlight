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
    val gtfsMap: GtfsMap
    val streetMap: StreetMap
    val eventCreator: EventCreator
}

interface ClientContext {
    val gtfs: GtfsBrowserClient
    val event: EventBrowserClient
    val location: LocationBrowserClient
}