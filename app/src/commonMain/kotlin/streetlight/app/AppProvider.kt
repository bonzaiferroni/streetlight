package streetlight.app

import kabinet.GEMINI_KEY
import kabinet.gemini.NeoGeminiClient
import pondui.io.NeoApiClient
import pondui.io.SpeechApiClient
import pondui.io.SpeechAppClient
import pondui.io.SpeechMockClient
import pondui.ui.services.WavePlayer
import streetlight.app.io.StreetApiClient
import streetlight.app.io.StreetMockClient
import streetlight.app.io.StreetRepository
import streetlight.app.io.EventApiClient
import streetlight.app.io.EventMockClient
import streetlight.app.io.EventRepository
import streetlight.app.io.LocationApiClient
import streetlight.app.io.LocationMockClient
import streetlight.app.io.LocationRepository
import streetlight.app.io.SongApiClient
import streetlight.app.io.SongMockClient
import streetlight.app.io.RenditionApiClient
import streetlight.app.io.RenditionMockClient
import streetlight.app.io.RenditionRepository
import streetlight.app.io.SongRepository
import streetlight.model.APP_API_URL
import streetlight.model.Api

interface AppProvider {
    val repo: AppClient
    // val dao: AppDao
    val speech: SpeechAppClient
    val wavePlayer: WavePlayer
}

interface AppClient {
    val area: StreetRepository
    val event: EventRepository
    val location: LocationRepository
    val song: SongRepository
    val rendition: RenditionRepository
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
    val apiClient = NeoApiClient(APP_API_URL)

    override val repo = object: AppClient {
        override val area = StreetApiClient(apiClient)
        override val event = EventApiClient(apiClient)
        override val location = LocationApiClient(apiClient)
        override val song = SongApiClient(apiClient)
        override val rendition = RenditionApiClient(apiClient)
    }
    override val speech = SpeechApiClient(Api.Speech, apiClient)
    override val wavePlayer = WavePlayer()

    val gemini = NeoGeminiClient(GEMINI_KEY)
}

object MockProvider: AppProvider {
    override val repo = object : AppClient {
        override val area = StreetMockClient()
        override val event = EventMockClient()
        override val location = LocationMockClient()
        override val song = SongMockClient()
        override val rendition = RenditionMockClient()
    }
    override val speech = SpeechMockClient()
    override val wavePlayer get() = error("no mock wave player")
}