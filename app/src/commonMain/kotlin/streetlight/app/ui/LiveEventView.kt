package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Instant
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import streetlight.model.data.*

@Composable
fun LiveEventView(
    startsAt: Instant,
    endsAt: Instant,
    status: EventStatus,
    setStatus: (EventStatus) -> Unit,
    viewModel: LiveEventModel = viewModel { LiveEventModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    Column(1) {
        EventStatusDash(
            startsAt = startsAt,
            endsAt = endsAt,
            breakStartedAt = state.breakStartedAt,
            status = status,
            setStatus = { status ->
                if (state.song == null && status == EventStatus.Live)
                    viewModel.takeNextSong()
                setStatus(status)
            },
            toggleBreak = viewModel::toggleBreak,
        )

        MagicItem(
            state.song,
            contentAlignment = Alignment.Center,
            isVisibleInit = true,
            offsetX = 50.dp,
            scale = .8f,
        ) { song ->
            val songPlay = state.songPlay
            if (song != null && songPlay != null) {
                TabSection {
                    Tab("Song") {
                        LiveSongDash(
                            title = song.title,
                            notes = songPlay.notes,
                            rating = songPlay.rating,
                            setRating = viewModel::setRating,
                            setNotes = viewModel::setNotes,
                            takeNextSong = viewModel::takeNextSong,
                        )
                    }
                    Tab("Chords") {
                        SongChordsDash()
                    }
                }
            } else {
                // show something
            }
        }
    }
}