package streetlight.app

import pondui.utils.ApiDao
import streetlight.app.io.AreaApiClient
import streetlight.app.io.EventApiClient
import streetlight.app.io.LocationApiClient
import streetlight.app.io.SongApiClient
import streetlight.app.io.SongPlayApiClient
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.NewEvent

interface AppProvider {
    val client: AppClient
    val dao: AppDao
}

class AppClient(
    val area: AreaApiClient = AreaApiClient(),
    val event: EventApiClient = EventApiClient(),
    val location: LocationApiClient = LocationApiClient(),
    val song: SongApiClient = SongApiClient(),
    val songPlay: SongPlayApiClient = SongPlayApiClient(),
)

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