package streetlight.web.model

import koala.model.GeoCamera
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient
import streetlight.web.io.OSMClient
import streetlight.web.io.OmniLog
import streetlight.web.io.TransitClient

@Deprecated("use dependency injection")
interface Streetlight {
    val appScope: CoroutineScope
    val client: ClientFacade
    val portal: Portal
    val gate: SessionGate
    // val gateAgent: GateAgent
    val geoMap: GeoCamera
    val markerMap: MarkerMap
    val chatRoom: ChatRoom
    val cache: DataCache
    val config: SiteConfig
    val omni: OmniLog
    val stage: StageFacade
    val toaster: Toaster
}

@Deprecated("use dependency injection")
interface ClientFacade {
    val transit: TransitClient
    val api: ApiClient
    val location: OSMClient
}

@Deprecated("use dependency injection")
interface StageFacade {
    val galaxy: GalaxyStage
}