package streetlight.web.model

import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.Event
import streetlight.model.data.EventStar
import streetlight.model.data.InterestType
import streetlight.web.io.ApiClient

class UserInterest(
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(UserInterestState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    private val _interestFlow = MutableSharedFlow<EventStar>(1)
    val interestFlow: Flow<EventStar> = _interestFlow

    fun editEventInterest(interest: EventStar) {
        scope.launch {
            val isSuccess = api.editEventInterest(interest) ?: return@launch
            if (isSuccess) {
                _interestFlow.emit(interest)

                when (interest.value) {
                    null -> {
                        val events = stateNow.events.filter { it.eventId != interest.eventId }
                        state.set { it.copy(events = events) }
                    }
                    else -> {
                        val event = api.readEvent(interest.eventId) ?: return@launch
                        val events = (stateNow.events + event).sortedBy { event.startsAt }
                        state.set { it.copy(events = events) }
                    }
                }
            }
        }
    }
}

data class UserInterestState(
    val events: List<Event> = emptyList()
)