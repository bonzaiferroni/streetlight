package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.EventFeedRoute
import streetlight.app.io.EventStore
import streetlight.model.data.Event

class EventFeedModel(
    route: EventFeedRoute,
    private val store: EventStore = EventStore()
): StateModel<EventFeedState>() {

    override val state = ModelState(EventFeedState())

    init {
        refreshItems()
    }

    fun refreshItems() {
        viewModelScope.launch {
            val events = store.readEventFeed() ?: return@launch
            setState { it.copy(events = events) }
        }
    }
}

data class EventFeedState(
    val events: List<Event> = listOf()
)