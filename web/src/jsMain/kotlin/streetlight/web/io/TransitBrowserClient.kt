package streetlight.web.io

import koala.external.FeedEntity
import streetlight.model.Api

class TransitBrowserClient(private val client: FetchClient) {
    suspend fun readAreaTransit() = client.get(Api.Gtfs.Routes)
    suspend fun readVehiclePositions(feedType: ProtobufType) =
        client.getProtobuf<FeedEntity>(Api.Gtfs.VehiclePosition.path, feedType)
    suspend fun readVehiclePositions(timestamp: Long) = client.get(Api.Gtfs.TransitState, EncodingType.Cbor) {
        param(it.timestamp, timestamp)
    }
}