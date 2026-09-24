package streetlight.web.io

import koala.external.FeedEntity
import streetlight.model.Api

/** The calls of `Api.Gtfs`, for the routes and live vehicles of the transit network. */
class TransitClient(private val client: FetchClient) {
    suspend fun readAreaTransit() = client.getApi(Api.Gtfs.Routes)
    suspend fun readVehiclePositions(feedType: ProtobufType) =
        client.getProtobuf<FeedEntity>(Api.Gtfs.VehiclePosition.path, feedType)
    suspend fun readVehiclePositions(timestamp: Long) = client.get(Api.Gtfs.TransitState, EncodingType.Cbor) {
        writeParam(it.timestamp, timestamp)
    }
}