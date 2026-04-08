package streetlight.app.ui

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Clock
import pondui.ui.controls.UpdateStatus
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.Song
import streetlight.model.data.SongId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SongProfileModel(
    songId: SongId,
    private val app: AppProvider = RuntimeProvider
) : StateModel<SongProfileState>() {
    override val state = ModelState(SongProfileState())
    private val repo = app.repo.song
    private var updateJob: Job? = null

    init {
        ioLaunch {
            val song = repo.readById(songId)
            setStateFromMain { it.copy(song = song, storedSong = song) }
        }
    }

    fun updateSong(song: Song, delay: Duration = 1.seconds) {
        val stored = stateNow.storedSong ?: return
        if (song == stored) return
        setState { it.copy(song = song)}
        updateJob?.cancel()
        updateJob = ioLaunch {
            delay(delay)
            setStateFromMain { it.copy(updateStatus = UpdateStatus.InProgress) }
            val update = song.copy(updatedAt = Clock.System.now())
            val isSuccess = repo.updateSong(update)
            setStateFromMain {
                when (isSuccess) {
                    true -> it.copy(song = update, storedSong = update, updateStatus = UpdateStatus.Done)
                    else -> it.copy(song = stored, updateStatus = UpdateStatus.Failed)
                }
            }
        }
    }
}

data class SongProfileState(
    val song: Song? = null,
    val storedSong: Song? = null,
    val updateStatus: UpdateStatus = UpdateStatus.None,
)