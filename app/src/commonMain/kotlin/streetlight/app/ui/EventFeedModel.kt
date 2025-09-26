package streetlight.app.ui

import kabinet.utils.toLocalDateTimeUtc
import kabinet.utils.toLongFormat
import kotlinx.datetime.Clock
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.Event
import streetlight.model.data.Location
import streetlight.model.data.NewEvent
import kotlin.time.Duration.Companion.days

class EventFeedModel(private val app: AppProvider = RuntimeProvider): StateModel<EventFeedState>() {

    private val client = app.repo.event

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

    fun create(location: Location) {
        ioLaunch {
            val startsAt = Clock.System.now() + 1.days
            val dayOfWeek = startsAt.toLocalDateTimeUtc().dayOfWeek.toLongFormat()
            val event = client.createEvent(
                NewEvent(
                    location.locationId,
                    "$dayOfWeek @ ${location.name}",
                    startsAt
                )
            ) ?: return@ioLaunch
            setStateFromMain { it.copy(events = it.events + event)}
        }
    }

    fun searchLocations(query: String) {
        ioLaunch {
            val locations = app.repo.location.search(query) ?: return@ioLaunch
            setStateFromMain { it.copy(locations = locations) }
        }
    }

    fun removeEvent(event: Event) {
        ioLaunch {
            val isSuccess = client.deleteEvent(event.eventId) ?: error("Error removing event")
            if (isSuccess) {
                setStateFromMain { it.copy(events = it.events - event)}
            }
        }
    }
}

data class EventFeedState(
    val locations: List<Location> = listOf(),
    val locationSearch: String = "",
    val events: List<Event> = listOf()
)