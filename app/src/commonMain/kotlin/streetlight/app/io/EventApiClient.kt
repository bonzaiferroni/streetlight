package streetlight.app.io

import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.NewEvent

class EventApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readEventFeed() = client.request(Api.EventFeed)
    suspend fun readById(eventId: EventId) = client.getById(Api.EventProfile, eventId)
    suspend fun createEvent(event: NewEvent) = client.request(Api.EventFeed.Create, event)
    suspend fun updateEvent(event: Event) = client.request(Api.EventProfile.Update, event)
    suspend fun deleteEvent(eventId: EventId) = client.request(Api.EventFeed.Delete, eventId)
}