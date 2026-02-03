package streetlight.web

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.CommunityId
import streetlight.model.data.AreaTransit
import kotlin.time.Duration.Companion.seconds

class GtfsMap(
    scope: CoroutineScope,
    val gtfsClient: GtfsBrowserClient
): BrowserModel<GtfsMapState>(GtfsMapState(), scope) {

    init {
        viewModelScope.launch {
            launch {
                val communityId = CommunityId.random()
                val areaTransit = gtfsClient.readAreaTransit()
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
        val feed = gtfsClient.readVehiclePositions(feedType)
        val timestamp = feed.header.timestamp.toString().toLong()
        val delta = (timestamp - stateNow.timestamp).toInt()
        console.log("fetching vehicles -- timestamp delta: $delta")
        if (delta == 0) return
        val vehiclePositions = feed.entity
            .mapNotNull { it.vehicle }
            .filter { it.trip != null }
            .toList()
        setState { it.copy(vehiclePositions = vehiclePositions, timestamp = timestamp)}
    }
}

data class GtfsMapState(
    val vehiclePositions: List<VehiclePosition> = emptyList(),
    val timestamp: Long = 0L,
    val areaTransit: AreaTransit? = null,
    val communityId: CommunityId? = null,
)