package streetlight.web

import streetlight.model.Api

class GtfsBrowserClient() {
    suspend fun readAreaTransit() = Api.Gtfs.Routes.get()
    suspend fun readVehiclePositions(feedType: ProtobufType) = Api.Gtfs.VehiclePosition.getProtobuf<FeedEntity>(feedType)
}