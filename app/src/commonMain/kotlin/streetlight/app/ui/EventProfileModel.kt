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
import kotlin.time.Duration.Companion.seconds

@Stable
class EventProfileModel(
    private val eventId: EventId,
    private val app: AppProvider = RuntimeProvider
) : StateModel<EventProfileState>() {
    override val state = ModelState(EventProfileState())

    private val client = app.repo.event
    private var updateJob: Job? = null

    init {
        ioLaunch {
            val event = client.readById(eventId)
            setStateFromMain { it.copy(event = event,) }
        }
    }

    fun updateEvent(event: Event) {
        var update = event
        val original = stateNow.event ?: return
        if (original == update) return
        setState { it.copy(event = update) }
        updateJob?.cancel()
        updateJob = ioLaunch {
            delay(1.seconds)
            setStateFromMain { it.copy(updateStatus = UpdateStatus.InProgress) }
            update = update.copy(updatedAt = Clock.System.now())
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