package streetlight.web

import streetlight.model.Api

class GtfsBrowserClient(app: AppContext): AppContext by app {
    suspend fun readAreaTransit() = get(Api.Gtfs.Routes)
    suspend fun readVehiclePositions(feedType: ProtobufType) = getProtobuf<FeedEntity>(Api.Gtfs.VehiclePosition, feedType)
}