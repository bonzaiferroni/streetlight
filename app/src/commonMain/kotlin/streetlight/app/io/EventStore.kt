package streetlight.app.io

import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewEvent

class EventStore(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readEventFeed() = client.request(Api.Events)
    suspend fun createEvent(newEvent: NewEvent) = client.request(Api.Events.Create, newEvent)
}