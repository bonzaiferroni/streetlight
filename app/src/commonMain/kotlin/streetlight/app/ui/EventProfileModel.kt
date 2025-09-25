package streetlight.app.ui

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
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

@Stable
class EventProfileModel(
    private val app: AppProvider = RuntimeProvider
) : StateModel<EventProfileState>() {
    override val state = ModelState(EventProfileState())

    private val client = app.client.event
    private var updateJob: Job? = null

    fun init(eventId: EventId) {
        if (eventId == stateNow.event?.eventId) return
        ioLaunch {
            val event = client.readById(eventId)
            setStateFromMain { it.copy(event = event) }
        }
    }

    fun setTitle(value: String) = updateEvent { it.copy(title = value) }
    fun setDescription(value: String) = updateEvent { it.copy(description = value) }
    fun setStartsAt(value: Instant) = updateEvent { it.copy(startsAt = value) }
    fun setEndsAt(value: Instant) = updateEvent { it.copy(endsAt = value) }

    fun updateEvent(delay: Duration = 1.seconds, toUpdate: (Event) -> Event) {
        val original = stateNow.event ?: return
        var update = toUpdate(original)
        if (original == update) return
        setState { it.copy(event = update)}
        updateJob?.cancel()
        updateJob = ioLaunch {
            delay(delay)
            setStateFromMain { it.copy(updateStatus = UpdateStatus.InProgress) }
            update = update.copy(updatedAt = Clock.System.now())
            val isSuccess = client.updateEvent(update)
            setStateFromMain {
                when (isSuccess) {
                    true -> it.copy(event = update, updateStatus = UpdateStatus.Done)
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