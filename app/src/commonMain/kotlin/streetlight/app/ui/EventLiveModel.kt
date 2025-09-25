package streetlight.app.ui

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import pondui.ui.controls.UpdateStatus
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.Event
import streetlight.model.data.EventStatus
import streetlight.model.data.Song
import streetlight.model.data.SongPlay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class EventLiveModel(
    event: Event,
    private val app: AppProvider = RuntimeProvider
) : StateModel<EventLiveState>() {
    override val state = ModelState(EventLiveState(event))
    private var updateJob: Job? = null
    private val client = app.client.event

    init {
        ioLaunch {
        }
    }

    fun setStatus(value: EventStatus) = updateEvent(0.seconds) { it.copy(status = value) }

    fun takeNextSong() {
        ioLaunch {
            val song = app.client.song.takeNextSong(Clock.System.now() - 30.days)
            setStateFromMain { it.copy(song = song) }
        }
    }

    private fun updateEvent(delay: Duration = 1.seconds, toUpdate: (Event) -> Event) {
        val original = stateNow.event
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

data class EventLiveState(
    val event: Event,
    val song: Song? = null,
    val updateStatus: UpdateStatus = UpdateStatus.None,
)
