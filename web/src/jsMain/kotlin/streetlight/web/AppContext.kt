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
    val eventProfile: EventProfile
    val streetMap: StreetMap
    val eventEditor: EventEditor
    val storyEditor: StoryEditor
}

interface ClientContext {
    val transit: TransitBrowserClient
    val api: ApiClient
    val location: OSMClient
}