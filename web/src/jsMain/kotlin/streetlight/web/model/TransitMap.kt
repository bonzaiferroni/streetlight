package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.getDataOrNull
import koala.SvgFile
import koala.model.LineMarker
import koala.model.MarkerId
import koala.external.VehiclePosition
import koala.model.Altitude
import koala.model.GeoMap
import koala.model.IconMarker
import koala.model.mapDistinct
import koala.model.storeOf
import koala.model.toGeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.AreaTransit
import streetlight.model.data.AreaTransitState
import streetlight.model.data.TransitRouteId
import streetlight.model.data.TransitVehicle
import streetlight.model.data.VehicleType
import streetlight.web.io.TransitClient
import kotlin.time.Duration.Companion.seconds

class TransitMap(
    private val scope: CoroutineScope,
    private val client: TransitClient,
    private val geoMap: GeoMap,
    private val config: SiteConfig,
) {
    private val state = storeOf(TransitMapState())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val isActiveFlow = stateFlow.mapDistinct { it.isActive }
    private val markerLayer = geoMap.getOrCreateLayer(MarkerLayerConfig.Transit)

    private var currentEntities: List<VehicleMarker> = emptyList()
    private var trackingJob: Job? = null
    private var transit: AreaTransit? = null
    private var currentRoutes: List<RouteMarker>? = null
    private var isInitialized: Boolean = false

    fun init() {
        if (isInitialized) return
        isInitialized = true
        scope.launch {
            config.showTransitFlow.collect {
                setIsActive(it)
            }
        }
    }

    private fun setIsActive(value: Boolean) {
        if (value) {
            startTracking()
        } else {
            stopTracking()
        }
        state.set { it.copy(isActive = value, timestamp = 0L) }
    }

    private fun startTracking() {
        if (trackingJob?.isActive == true) return
        markerLayer.setIsVisible(true)
        trackingJob = scope.launch {
            val transit = readTransit() ?: return@launch

            val routes = currentRoutes
            if (routes != null) {
                // val layerIds = routes.map { it.layerId }.toSet().toList()
                // geoMap.showLayers(layerIds)
            } else {
                currentRoutes = createRoutes(transit)
            }

            delay(1.seconds)

            var timestamp = 0L

            while (true) {
                if (geoMap.camera.stateNow.isViewed) {
                    val transitState = client.readVehiclePositions(timestamp)
                    if (transitState != null) {
                        timestamp = transitState.timestamp
                        showTransitState(transitState)
                    }
                    delay(10.seconds)
                } else {
                    delay(1.seconds)
                }
            }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        markerLayer.setIsVisible(false)
        currentEntities = emptyList()
    }

    private suspend fun readTransit() = transit ?: client.readAreaTransit().getDataOrNull().also { transit = it }

    private fun createRoutes(transit: AreaTransit): List<RouteMarker> {
        val routes = transit.routes.mapNotNull { route ->
            if (route.vehicleType == VehicleType.Bus) return@mapNotNull null
            val vehicleType = route.vehicleType ?: return@mapNotNull null
            RouteMarker(route.transitRouteId, route.shortName, vehicleType, listOf(route.points))
        }
        markerLayer.setLines(routes)
        return routes
    }

    private fun showTransitState(transitState: AreaTransitState) {
        val transit = transit ?: return
        val vehicles = transitState.vehicles
//        val removedIds = currentEntities
//            .filter { currentEntity -> vehicles.none { currentEntity.vehicleId == it.vehicleId } }
//            .map { it.markerId }
        val markers = transitState.vehicles.map {
            val vehicleType = transit.routes.firstOrNull() { route -> route.transitRouteId.value == it.routeId }
                ?.vehicleType ?: VehicleType.Bus
            it.toMarker(transitState.timestamp, vehicleType)
        }
//        geoMap.removeEntities(removedIds)
        markerLayer.setPoints(markers)

        currentEntities = markers

        state.set { it.copy(timestamp = transitState.timestamp) }
    }
}

data class TransitMapState(
    val timestamp: Long = 0L,
    val isActive: Boolean = false,
)

data class RouteMarker(
    val transitRouteId: TransitRouteId,
    override val label: String,
    val vehicleType: VehicleType,
    override val lines: List<List<GeoPoint>>
): LineMarker {
    override val markerId: MarkerId get() = transitRouteId.value
//    override val layerId: LayerIdProto
//        get() = when(vehicleType) {
//        VehicleType.Bus -> "bus-layer"
//        VehicleType.LightRail -> "light-rail-layer"
//        VehicleType.Train -> "train-layer"
//    }
}

data class VehicleMarker(
    val vehicleId: String,
    override val label: String,
    override val geoPoint: GeoPoint,
    val vehicleType: VehicleType,
    override val opacity: Float,
    override val bearing: Float?,
): IconMarker {
    override val markerId get() = vehicleId
    override val icon get() = when (vehicleType) {
        VehicleType.Bus -> SvgFile.Bus
        VehicleType.LightRail -> SvgFile.Train
        VehicleType.Train -> SvgFile.Train
    }
    override val altitude get() = Altitude.Raincloud
}

fun VehiclePosition.toMarker(currentTime: Long, vehicleType: VehicleType): VehicleMarker? {
    val vehicleId = vehicle?.id ?: return null
    val position = position ?: return null
    val vehicleTime = timestamp.toString().toLong()
    val secondsSinceCapture = (currentTime - vehicleTime).toInt()
    val opacity = (1 - secondsSinceCapture / 240f).coerceIn(.5f, 1f)
    return VehicleMarker(
        vehicleId = vehicleId,
        label = trip?.routeId ?: "Transit",
        geoPoint = position.toGeoPoint(),
        vehicleType = vehicleType,
        opacity = opacity,
        bearing = position.bearing
    )
}

fun TransitVehicle.toMarker(currentTime: Long, vehicleType: VehicleType): VehicleMarker {
    val vehicleTime = timestamp.toString().toLong()
    val secondsSinceCapture = (currentTime - vehicleTime).toInt()
    val opacity = (1 - secondsSinceCapture / 240f).coerceIn(.5f, 1f)
    return VehicleMarker(
        vehicleId = vehicleId,
        label = routeId,
        geoPoint = geoPoint,
        vehicleType = vehicleType,
        opacity = opacity,
        bearing = bearing
    )
}