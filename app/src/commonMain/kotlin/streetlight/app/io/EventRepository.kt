package streetlight.app.io

import pondui.io.NeoApiClient
import streetlight.app.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.NewEvent
import streetlight.model.mockDb

interface EventRepository {
    suspend fun readEventFeed(): List<Event>?
    suspend fun readById(eventId: EventId): Event?
    suspend fun createEvent(event: NewEvent): Event?
    suspend fun updateEvent(event: Event): Boolean?
    suspend fun deleteEvent(eventId: EventId): Boolean?
}

class EventApiClient(
    private val client: NeoApiClient = globalNeoApiClient
): EventRepository {
    override suspend fun readEventFeed() = client.request(Api.EventFeed)
    override suspend fun readById(eventId: EventId) = client.getById(Api.EventProfile, eventId)
    override suspend fun createEvent(event: NewEvent) = client.request(Api.EventFeed.Create, event)
    override suspend fun updateEvent(event: Event) = client.request(Api.EventProfile.Update, event)
    override suspend fun deleteEvent(eventId: EventId) = client.request(Api.EventFeed.Delete, eventId)
}

class EventMockClient: EventRepository {
    override suspend fun readEventFeed(): List<Event>? = mockDb.events
    override suspend fun readById(eventId: EventId): Event? = mockDb.events.firstOrNull { it.eventId == eventId }
    override suspend fun createEvent(event: NewEvent): Event? = TODO("Not yet implemented")
    override suspend fun updateEvent(event: Event): Boolean? = TODO("Not yet implemented")
    override suspend fun deleteEvent(eventId: EventId): Boolean? = TODO("Not yet implemented")
}