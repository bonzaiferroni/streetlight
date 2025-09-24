package streetlight.app.ui

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import pondui.ui.controls.UpdateStatus
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.EventStatus
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class EventProfileModel(
    eventId: EventId,
    private val app: AppProvider = RuntimeProvider
) : StateModel<EventProfileState>() {
    override val state = ModelState(EventProfileState())

    private val client = app.client.event
    private var updateJob: Job? = null

    init {
        ioLaunch {
            val event = client.readById(eventId)
            setStateFromMain { it.copy(event = event) }
        }
    }

    fun setStatus(value: EventStatus) = updateEvent(0.seconds) { it.copy(status = value) }
    fun setTitle(value: String) = updateEvent { it.copy(title = value) }
    fun setDescription(value: String) = updateEvent { it.copy(description = value) }
    fun setStartsAt(value: Instant) = updateEvent { it.copy(startsAt = value) }
    fun setEndsAt(value: Instant) = updateEvent { it.copy(endsAt = value) }

    private fun updateEvent(delay: Duration = 1.seconds, toUpdate: (Event) -> Event) {
        val original = stateNow.event ?: return
        val update = toUpdate(original)
        if (original == update) return
        setState { it.copy(event = update)}
        updateJob?.cancel()
        updateJob = ioLaunch {
            delay(delay)
            setStateFromMain { it.copy(updateStatus = UpdateStatus.InProgress) }
            val isSuccess = client.updateEvent(update)
            setStateFromMain {
                when (isSuccess) {
                    true -> it.copy(updateStatus = UpdateStatus.Done)
                    else -> it.copy(event = original, updateStatus = UpdateStatus.Failed)
                }
            }
        }
    }
}

data class EventProfileState(
    val event: Event? = null,
    val updateStatus: UpdateStatus = UpdateStatus.None,
)