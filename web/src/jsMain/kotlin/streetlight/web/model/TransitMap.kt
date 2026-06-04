package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.getDataOrNull
import koala.SvgFile
import koala.css.modify
import koala.model.GeoMap
import koala.model.LayerId
import koala.model.LineMarker
import koala.model.MarkerId
import koala.model.PointMarker
import koala.external.VehiclePosition
import koala.model.MarkerUtility
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
import streetlight.web.io.ProtobufType
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

    private var currentEntities: List<TransitEntity> = emptyList()
    private var trackingJob: Job? = null
    private var transit: AreaTransit? = null
    private var currentRoutes: List<RouteEntity>? = null
    private var feedType: ProtobufType? = null
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
        trackingJob = scope.launch {
            val transit = readTransit() ?: return@launch

            val routes = currentRoutes
            if (routes != null) {
                val layerIds = routes.map { it.layerId }.toSet().toList()
                geoMap.showLayers(layerIds)
            } else {
                currentRoutes = createRoutes(transit)
            }

            delay(1.seconds)

            var timestamp = 0L

            while (true) {
                if (geoMap.stateNow.isViewed) {
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
        geoMap.removeEntities(currentEntities.map { it.markerId })
        currentEntities = emptyList()
        currentRoutes?.let { routes ->
            val layerIds = routes.map { it.layerId }.toSet().toList()
            geoMap.hideLayers(layerIds)
        }
    }

    private suspend fun readTransit() = transit ?: client.readAreaTransit().getDataOrNull().also { transit = it }

    private fun createRoutes(transit: AreaTransit): List<RouteEntity> {
        val routes = transit.routes.mapNotNull { route ->
            if (route.vehicleType == VehicleType.Bus) return@mapNotNull null
            val vehicleType = route.vehicleType ?: return@mapNotNull null
            RouteEntity(route.transitRouteId, route.shortName, vehicleType, route.points)
        }
        geoMap.addLines(routes)
        return routes
    }

    private fun showTransitState(transitState: AreaTransitState) {
        val transit = transit ?: return
        val vehicles = transitState.vehicles
        val removedIds = currentEntities
            .filter { currentEntity -> vehicles.none { currentEntity.vehicleId == it.vehicleId } }
            .map { it.markerId }
        val entities = transitState.vehicles.map {
            val vehicleType = transit.routes.firstOrNull() { route -> route.transitRouteId.value == it.routeId }
                ?.vehicleType ?: VehicleType.Bus
            it.toEntity(transitState.timestamp, vehicleType)
        }
        geoMap.removeEntities(removedIds)
        geoMap.addEntities(entities)

        currentEntities = entities

        state.set { it.copy(timestamp = transitState.timestamp) }
    }

    @Deprecated("Use showTransitState")
    private suspend fun fetchVehicles(feedType: ProtobufType, transit: AreaTransit) {
        val feed = client.readVehiclePositions(feedType) ?: return
        val timestamp = feed.header.timestamp.toString().toLong()
        val delta = (timestamp - stateNow.timestamp).toInt()
        console.log("fetching vehicles -- timestamp delta: $delta")
        if (delta == 0) return
        val entities = feed.entity.mapNotNull { feedEntity ->
            val vehicle = feedEntity.vehicle ?: return@mapNotNull null
            val trip = vehicle.trip ?: return@mapNotNull null
            val vehicleType = transit.routes.firstOrNull() { it.transitRouteId.value == trip.routeId }
                ?.vehicleType ?: VehicleType.Bus // return@mapNotNull null

            vehicle.toEntity(timestamp, vehicleType)
        }

        val removedIds = currentEntities
            .filter { currentEntity -> entities.none { currentEntity.vehicleId == it.vehicleId } }
            .map { it.markerId }

        if (removedIds.isNotEmpty()) {
            console.log("removing ${removedIds.size} vehicles")
        }

        geoMap.removeEntities(removedIds)
        geoMap.addEntities(entities)

        currentEntities = entities

        state.set { it.copy(timestamp = timestamp) }
    }
}

data class TransitMapState(
    val timestamp: Long = 0L,
    val isActive: Boolean = false,
)

data class RouteEntity(
    val transitRouteId: TransitRouteId,
    override val label: String,
    val vehicleType: VehicleType,
    override val points: List<GeoPoint>
): LineMarker {
    override val markerId: MarkerId get() = transitRouteId.value
    override val layerId: LayerId
        get() = when(vehicleType) {
        VehicleType.Bus -> "bus-layer"
        VehicleType.LightRail -> "light-rail-layer"
        VehicleType.Train -> "train-layer"
    }
}

data class TransitEntity(
    val vehicleId: String,
    override val label: String,
    override val geoPoint: GeoPoint,
    val vehicleType: VehicleType,
    override val opacity: Float,
    override val bearing: Float?,
): PointMarker {
    override val markerId get() = vehicleId
    override val icon get() = when (vehicleType) {
        VehicleType.Bus -> SvgFile.Bus
        VehicleType.LightRail -> SvgFile.Train
        VehicleType.Train -> SvgFile.Train
    }
    override val modifiers get() = modify(MarkerUtility.twinkleAboveAirplane)
}

fun VehiclePosition.toEntity(currentTime: Long, vehicleType: VehicleType): TransitEntity? {
    val vehicleId = vehicle?.id ?: return null
    val position = position ?: return null
    val vehicleTime = timestamp.toString().toLong()
    val secondsSinceCapture = (currentTime - vehicleTime).toInt()
    val opacity = (1 - secondsSinceCapture / 240f).coerceIn(.5f, 1f)
    return TransitEntity(
        vehicleId = vehicleId,
        label = trip?.routeId ?: "Transit",
        geoPoint = position.toGeoPoint(),
        vehicleType = vehicleType,
        opacity = opacity,
        bearing = position.bearing
    )
}

fun TransitVehicle.toEntity(currentTime: Long, vehicleType: VehicleType): TransitEntity {
    val vehicleTime = timestamp.toString().toLong()
    val secondsSinceCapture = (currentTime - vehicleTime).toInt()
    val opacity = (1 - secondsSinceCapture / 240f).coerceIn(.5f, 1f)
    return TransitEntity(
        vehicleId = vehicleId,
        label = routeId,
        geoPoint = geoPoint,
        vehicleType = vehicleType,
        opacity = opacity,
        bearing = bearing
    )
}