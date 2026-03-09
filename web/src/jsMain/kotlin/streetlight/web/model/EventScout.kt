package streetlight.web.model

import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.EventEdit
import streetlight.web.ui.ViewModel

class EventScout(
    override val app: Streetlight,
    val scope: CoroutineScope,
): ViewModel {
    private val state = storeOf(EventScoutState())
    val stateFlow = state.flow
    val stateNow get() = state.now
}

data class EventScoutState(
    val event: EventEdit = EventEdit()
)