package streetlight.web.model

import kampfire.model.GeoPoint
import koala.css.modify
import koala.model.BrowserModel
import koala.model.GeoMap
import koala.model.LayerId
import koala.model.LineEntity
import koala.model.MapEntityId
import koala.model.PointEntity
import koala.external.VehiclePosition
import koala.model.MarkerUtility
import koala.model.toGeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.CommunityId
import streetlight.model.data.AreaTransit
import streetlight.model.data.TransitRouteId
import streetlight.model.data.VehicleType
import streetlight.web.io.ProtobufType
import streetlight.web.io.SvgPath
import streetlight.web.io.protobuf
import kotlin.time.Duration.Companion.seconds

class TransitMap(
    scope: CoroutineScope,
    private val client: ClientContext,
    private val geoMap: GeoMap,
) : BrowserModel<TransitMapState>(TransitMapState(), scope) {

    init {
        scope.launch {
            val communityId = CommunityId.random()
            val areaTransit = client.transit.readAreaTransit()
            showRoutes(areaTransit)
            setState { it.copy(areaTransit = areaTransit, communityId = communityId) }
            val root = protobuf.load("/www/proto/gtfs-realtime.proto").await()
            val feedType = root.lookupType("transit_realtime.FeedMessage")

            delay(1.seconds)

            while (true) {
                if (geoMap.stateNow.isViewed) {
                    fetchVehicles(feedType)
                    delay(30.seconds)
                } else {
                    delay(1.seconds)
                }
            }
        }
    }

    private fun showRoutes(transit: AreaTransit?) {
        val transit = transit ?: return
        val lines = transit.routes.mapNotNull { route ->
            if (route.vehicleType == VehicleType.Bus) return@mapNotNull null
            val vehicleType = route.vehicleType ?: return@mapNotNull null
            RouteEntity(route.transitRouteId, route.shortName, vehicleType, route.points)
        }
        geoMap.addLines(lines)
    }

    private suspend fun fetchVehicles(feedType: ProtobufType) {
        val feed = client.transit.readVehiclePositions(feedType)
        val timestamp = feed.header.timestamp.toString().toLong()
        val delta = (timestamp - stateNow.timestamp).toInt()
        console.log("fetching vehicles -- timestamp delta: $delta")
        if (delta == 0) return
        val entities = feed.entity.mapNotNull { feedEntity ->
            val vehicle = feedEntity.vehicle ?: return@mapNotNull null
            val trip = vehicle.trip ?: return@mapNotNull null
            val vehicleType = stateNow.areaTransit?.routes?.firstOrNull() { it.transitRouteId.value == trip.routeId }
                ?.vehicleType ?: return@mapNotNull null // VehicleType.Bus

            vehicle.toEntity(timestamp, vehicleType)
        }

        val removedIds = stateNow.entities
            .filter { currentEntity -> entities.none { currentEntity.vehicleId == it.vehicleId } }
            .map { it.vehicleId }

        if (removedIds.isNotEmpty()) {
            console.log("removing ${removedIds.size} vehicles")
        }

        geoMap.removeEntities(removedIds)
        geoMap.addEntities(entities)

        setState { it.copy(entities = entities, timestamp = timestamp) }
    }
}

data class TransitMapState(
    val entities: List<TransitEntity> = emptyList(),
    val timestamp: Long = 0L,
    val areaTransit: AreaTransit? = null,
    val communityId: CommunityId? = null,
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