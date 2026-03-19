package streetlight.web.model

import kampfire.model.GeoPoint
import koala.css.modify
import koala.model.GeoMap
import koala.model.LayerId
import koala.model.LineEntity
import koala.model.MapEntityId
import koala.model.PointEntity
import koala.external.VehiclePosition
import koala.model.MarkerUtility
import koala.model.mapDistinct
import koala.model.storeOf
import koala.model.toGeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.AreaTransit
import streetlight.model.data.TransitRouteId
import streetlight.model.data.VehicleType
import streetlight.web.io.ProtobufType
import streetlight.web.ui.SvgPath
import streetlight.web.io.protobuf
import kotlin.time.Duration.Companion.seconds

class TransitMap(
    private val scope: CoroutineScope,
    private val client: ClientContext,
    private val geoMap: GeoMap,
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

    fun setIsActive(value: Boolean) {
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

            val feedType = feedType ?: protobuf.load("/www/proto/gtfs-realtime.proto").await()
                .lookupType("transit_realtime.FeedMessage").also { feedType = it }

            delay(1.seconds)

            while (true) {
                if (geoMap.stateNow.isViewed) {
                    fetchVehicles(feedType, transit)
                    delay(30.seconds)
                } else {
                    delay(1.seconds)
                }
            }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        geoMap.removeEntities(currentEntities.map { it.entityId })
        currentEntities = emptyList()
        currentRoutes?.let { routes ->
            val layerIds = routes.map { it.layerId }.toSet().toList()
            geoMap.hideLayers(layerIds)
        }
    }

    private suspend fun readTransit() = transit ?: client.transit.readAreaTransit().also { transit = it }

    private fun createRoutes(transit: AreaTransit): List<RouteEntity> {
        val routes = transit.routes.mapNotNull { route ->
            if (route.vehicleType == VehicleType.Bus) return@mapNotNull null
            val vehicleType = route.vehicleType ?: return@mapNotNull null
            RouteEntity(route.transitRouteId, route.shortName, vehicleType, route.points)
        }
        geoMap.addLines(routes)
        return routes
    }

    private suspend fun fetchVehicles(feedType: ProtobufType, transit: AreaTransit) {
        val feed = client.transit.readVehiclePositions(feedType)
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
            .map { it.entityId }

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
): LineEntity {
    override val entityId: MapEntityId get() = transitRouteId.value
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
    override val position: GeoPoint,
    val vehicleType: VehicleType,
    override val opacity: Float,
    override val bearing: Float?,
): PointEntity {
    override val entityId get() = vehicleId
    override val iconPath get() = when (vehicleType) {
        VehicleType.Bus -> SvgPath.bus
        VehicleType.LightRail -> SvgPath.train
        VehicleType.Train -> SvgPath.train
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
        position = position.toGeoPoint(),
        vehicleType = vehicleType,
        opacity = opacity,
        bearing = position.bearing
    )
}