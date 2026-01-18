package streetlight.web

import streetlight.model.Api
import streetlight.model.data.TransitRoute

class GtfsBrowserClient() {
    suspend fun readRoutes() = Api.Gtfs.Routes.get()

    suspend fun readVehiclePositions(feedType: ProtobufType) = Api.Gtfs.VehiclePosition.getProtobuf<FeedEntity>(feedType)
}