package streetlight.app.ui

import kotlinx.datetime.Clock
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.Event
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.NewEvent

class EventFeedModel(private val app: AppProvider = RuntimeProvider): StateModel<EventFeedState>() {

    private val client = app.client.event

    override val state = ModelState(EventFeedState())

    init {
        ioLaunch {
            refreshEvents()
        }
    }

    private suspend fun refreshEvents() {
        val events = client.readEventFeed() ?: return
        setStateFromMain { it.copy(events = events) }
    }

    fun create(locationId: LocationId) {
        ioLaunch {
            val event = client.createEvent(NewEvent(locationId, Clock.System.now())) ?: return@ioLaunch
            setStateFromMain { it.copy(events = it.events + event)}
        }
    }

    fun searchLocations(query: String) {
        ioLaunch {
            val locations = app.client.location.search(query) ?: return@ioLaunch
            setStateFromMain { it.copy(locations = locations) }
        }
    }
}

data class EventFeedState(
    val locations: List<Location> = listOf(),
    val locationSearch: String = "",
    val events: List<Event> = listOf()
)