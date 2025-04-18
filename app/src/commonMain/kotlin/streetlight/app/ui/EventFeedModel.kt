package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import pondui.ui.core.StateModel
import streetlight.app.EventFeedRoute
import streetlight.app.io.EventStore
import streetlight.model.data.Event

class EventFeedModel(
    route: EventFeedRoute,
    store: EventStore = EventStore()
): StateModel<EventFeedState>(EventFeedState()) {
    init {
        viewModelScope.launch {
            val events = store.readEventFeed()
                .toImmutableList()
            setState { it.copy(events = events) }
        }
    }
}

data class EventFeedState(
    val events: ImmutableList<Event> = persistentListOf()
)