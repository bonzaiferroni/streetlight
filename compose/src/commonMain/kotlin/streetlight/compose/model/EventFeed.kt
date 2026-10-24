package streetlight.compose.model

import androidx.lifecycle.ViewModel
import kampfire.model.storeOf
import kampfire.model.tapOf
import streetlight.model.data.Event

/** The events shown on [EventFeedRoute]. */
class EventFeed : ViewModel() {
    private val store = storeOf(EventFeedState())

    val eventsState = store.tapOf { it.events }
}

/** The state of an [EventFeed]. */
data class EventFeedState(
    val events: List<Event> = emptyList(),
)
