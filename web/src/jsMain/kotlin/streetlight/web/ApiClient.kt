package streetlight.web

import kampfire.model.GeoPoint
import streetlight.model.Api
import streetlight.model.data.EventId
import streetlight.model.data.MapQuery
import streetlight.model.data.NewEvent
import streetlight.model.data.NewLocation
import streetlight.model.data.UserFileRequest

class ApiClient(app: AppContext): AppContext by app {
    suspend fun readEvent(eventId: EventId) = get(Api.EventProfile, eventId)
    suspend fun readEventFeed() = get(Api.Events)
    suspend fun queryMap(request: MapQuery) = get(Api.Events.QueryMap, request.toQuery())
    suspend fun create(event: NewEvent) = post(Api.Events.Create, event)
    suspend fun uploadFeatureImage(blobUrl: String) = uploadBlob(Api.Events.Upload.path, blobUrl)

    suspend fun readUserFiles(request: UserFileRequest) = post(Api.Users.Images, request)

    suspend fun createLocation(newLocation: NewLocation) = post(Api.LocationFeed.Create, newLocation)
    suspend fun queryLocation(point: GeoPoint) = get(Api.LocationFeed.QueryPoint, point.toQuery())
}

//    suspend fun readById(eventId: EventId) = client.getById(Api.EventProfile, eventId)
//    suspend fun updateEvent(event: Event) = client.request(Api.EventProfile.Update, event)
//    suspend fun deleteEvent(eventId: EventId) = client.request(Api.EventFeed.Delete, eventId)