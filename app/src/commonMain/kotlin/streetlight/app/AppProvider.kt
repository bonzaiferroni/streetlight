package streetlight.app

import pondui.utils.ApiDao
import streetlight.app.io.AreaApiClient
import streetlight.app.io.AreaMockClient
import streetlight.app.io.AreaRepository
import streetlight.app.io.EventApiClient
import streetlight.app.io.EventMockClient
import streetlight.app.io.EventRepository
import streetlight.app.io.LocationApiClient
import streetlight.app.io.LocationMockClient
import streetlight.app.io.LocationRepository
import streetlight.app.io.SongApiClient
import streetlight.app.io.SongMockClient
import streetlight.app.io.SongPlayApiClient
import streetlight.app.io.SongPlayMockClient
import streetlight.app.io.SongPlayRepository
import streetlight.app.io.SongRepository
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.NewEvent

interface AppProvider {
    val client: AppClient
    // val dao: AppDao
}

interface AppClient {
    val area: AreaRepository
    val event: EventRepository
    val location: LocationRepository
    val song: SongRepository
    val songPlay: SongPlayRepository
}

class AppDao(
    client: AppClient,
//    val event: ApiDao<Event, EventId, NewEvent> = ApiDao(
//        kClass = Event::class,
//        provideId = { it.eventId },
//        apiReadAll = client.event::readEventFeed,
//        apiCreate = client.event::createEvent,
//        apiUpdate = client.event::updateEvent,
//        apiDelete = client.event::deleteEvent,
//    ),
)

object RuntimeProvider: AppProvider {
    override val client = object: AppClient {
        override val area = AreaApiClient()
        override val event = EventApiClient()
        override val location = LocationApiClient()
        override val song = SongApiClient()
        override val songPlay = SongPlayApiClient()
    }
}

object PreviewProvider: AppProvider {
    override val client = object : AppClient {
        override val area = AreaMockClient()
        override val event = EventMockClient()
        override val location = LocationMockClient()
        override val song = SongMockClient()
        override val songPlay = SongPlayMockClient()
    }
}