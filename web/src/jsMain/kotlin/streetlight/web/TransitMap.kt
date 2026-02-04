package streetlight.web

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.CommunityId
import streetlight.model.data.AreaTransit
import kotlin.time.Duration.Companion.seconds

class TransitMap(
    scope: CoroutineScope,
    private val client: ClientContext,
    private val geoMap: GeoMap,
) : BrowserModel<TransitMapState>(TransitMapState(), scope) {

    init {
        viewModelScope.launch {
            launch {
                val communityId = CommunityId.random()
                val areaTransit = client.transit.readAreaTransit()
                setState { it.copy(areaTransit = areaTransit, communityId = communityId) }
                val root = protobuf.load("/www/proto/gtfs-realtime.proto").await()
                val feedType = root.lookupType("transit_realtime.FeedMessage")

                while (true) {
                    fetchVehicles(feedType)
                    delay(30.seconds)
                }
            }
        }
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
                ?.vehicleType ?: return@mapNotNull null

            vehicle.toEntity(timestamp, vehicleType)
        }

        val removedIds = stateNow.entities
            .filter { currentEntity -> entities.none { currentEntity.vehicleId != it.vehicleId } }
            .map { it.vehicleId }

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