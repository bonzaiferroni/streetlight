package streetlight.app.io

import pondui.io.ApiStore
import streetlight.model.Api
import streetlight.model.data.NewEvent

class EventStore: ApiStore() {
    suspend fun readEventFeed() = client.get(Api.Events)
    suspend fun createEvent(newEvent: NewEvent) = client.post(Api.Events.Create, newEvent)
}