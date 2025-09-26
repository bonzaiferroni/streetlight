package streetlight.app.ui

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.NewSongPlay
import streetlight.model.data.SelfRating
import streetlight.model.data.Song
import kotlin.time.Duration.Companion.days

class LiveEventModel(
    private val app: AppProvider = RuntimeProvider
) : StateModel<LiveEventState>() {
    override val state = ModelState(LiveEventState())
    private val client = app.repo.event

    init {
        ioLaunch {
        }
    }

    fun takeNextSong() {
        ioLaunch {
            stateNow.songPlay?.let {
                app.repo.songPlay.create(it)
            }

            val song = app.repo.song.takeNextSong(Clock.System.now() - 30.days) ?: return@ioLaunch
            val newSongPlay = NewSongPlay(
                songId = song.songId,
                notes = null,
                rating = null,
            )
            setStateFromMain { it.copy(song = song, songPlay = newSongPlay) }
        }
    }

    fun setRating(rating: SelfRating?) {
        val songPlay = stateNow.songPlay ?: return
        setState { it.copy(songPlay = songPlay.copy(rating = rating))}
    }

    fun setNotes(notes: String) {
        val songPlay = stateNow.songPlay ?: return
        setState { it.copy(songPlay = songPlay.copy(notes = notes.takeIf { it.isNotBlank() }))}
    }

//    fun addSongPlay(rating: SelfRating, takeNext: Boolean) {
//        val songId = stateNow.song?.songId ?: return
//        val notes = stateNow.songPlayNotes.takeIf { it.isNotEmpty() }
//        ioLaunch {
//            app.client.songPlay.create(NewSongPlay(
//                songId = songId,
//                notes = notes,
//                rating = rating
//            ))
//            if (takeNext) takeNextSong()
//        }
//    }

    fun toggleBreak() {
        if (stateNow.breakStartedAt != null) {
            setState { it.copy(breakStartedAt = null)}
        } else {
            setState { it.copy(breakStartedAt = Clock.System.now()) }
        }
    }
}

data class LiveEventState(
    val song: Song? = null,
    val songPlay: NewSongPlay? = null,
    val breakStartedAt: Instant? = null,
)
