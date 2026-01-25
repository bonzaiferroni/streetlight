package streetlight.web

import streetlight.model.Api
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.LocationEventsRequest
import streetlight.model.data.NewEvent

class EventBrowserClient() {
    suspend fun readEventFeed() = Api.EventFeed.get()
    suspend fun readLocationEvents(request: LocationEventsRequest) = Api.EventFeed.LocationEvents.get(request.toQuery())
//    suspend fun readById(eventId: EventId) = client.getById(Api.EventProfile, eventId)
//    suspend fun createEvent(event: NewEvent) = client.request(Api.EventFeed.Create, event)
//    suspend fun updateEvent(event: Event) = client.request(Api.EventProfile.Update, event)
//    suspend fun deleteEvent(eventId: EventId) = client.request(Api.EventFeed.Delete, eventId)
}