package streetlight.web

import streetlight.model.Api
import streetlight.model.data.MapQuery

class EventBrowserClient() {
    suspend fun readEventFeed() = Api.EventFeed.get()
    suspend fun queryMap(request: MapQuery) = Api.EventFeed.QueryMap.get(request.toQuery())
//    suspend fun readById(eventId: EventId) = client.getById(Api.EventProfile, eventId)
//    suspend fun createEvent(event: NewEvent) = client.request(Api.EventFeed.Create, event)
//    suspend fun updateEvent(event: Event) = client.request(Api.EventProfile.Update, event)
//    suspend fun deleteEvent(eventId: EventId) = client.request(Api.EventFeed.Delete, eventId)
}