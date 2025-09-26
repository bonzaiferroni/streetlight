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
import streetlight.model.data.NewSongPlay
import streetlight.model.data.SelfRating
import streetlight.model.data.Song
import streetlight.model.data.SongPlay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class EventLiveModel(
    private val app: AppProvider = RuntimeProvider
) : StateModel<EventLiveState>() {
    override val state = ModelState(EventLiveState())
    private val client = app.client.event

    init {
        ioLaunch {
        }
    }

    fun takeNextSong() {
        ioLaunch {
            val song = app.client.song.takeNextSong(Clock.System.now() - 30.days)
            setStateFromMain { it.copy(song = song, songPlayNotes = "") }
        }
    }

    fun addSongPlay(rating: SelfRating, takeNext: Boolean) {
        val songId = stateNow.song?.songId ?: return
        val notes = stateNow.songPlayNotes.takeIf { it.isNotEmpty() }
        ioLaunch {
            app.client.songPlay.create(NewSongPlay(
                songId = songId,
                notes = notes,
                rating = rating
            ))
            if (takeNext) takeNextSong()
        }
    }
}

data class EventLiveState(
    val song: Song? = null,
    val songPlayNotes: String = "",
)
