package streetlight.web

import streetlight.model.Api
import streetlight.model.data.MapQuery

class EventBrowserClient(app: AppContext): AppContext by app {
    suspend fun readEventFeed() = get(Api.EventFeed)
    suspend fun queryMap(request: MapQuery) = get(Api.EventFeed.QueryMap, request.toQuery())
    suspend fun readSecure() = get(Api.LocationFeed.Secure)
//    suspend fun readById(eventId: EventId) = client.getById(Api.EventProfile, eventId)
//    suspend fun createEvent(event: NewEvent) = client.request(Api.EventFeed.Create, event)
//    suspend fun updateEvent(event: Event) = client.request(Api.EventProfile.Update, event)
//    suspend fun deleteEvent(eventId: EventId) = client.request(Api.EventFeed.Delete, eventId)
}