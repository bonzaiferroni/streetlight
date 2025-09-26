package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*
import streetlight.model.mockDb
import kotlin.time.Duration.Companion.minutes

@Composable
fun LiveEventView(
    startsAt: Instant,
    endsAt: Instant,
    status: EventStatus,
    setStatus: (EventStatus) -> Unit,
    viewModel: LiveEventModel = viewModel { LiveEventModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    LiveEventDash(
        startsAt = startsAt,
        endsAt = endsAt,
        breakStartedAt = state.breakStartedAt,
        status = status,
        song = state.song,
        songPlay = state.songPlay,
        setStatus = { status ->
            if (state.song == null && status == EventStatus.Live)
                viewModel.takeNextSong()
            setStatus(status)
        },
        setRating = viewModel::setRating,
        setNotes = viewModel::setNotes,
        toggleBreak = viewModel::toggleBreak,
        takeNextSong = viewModel::takeNextSong
    )
}

@Composable
fun LiveEventDash(
    startsAt: Instant,
    endsAt: Instant,
    breakStartedAt: Instant?,
    status: EventStatus,
    song: Song?,
    songPlay: NewSongPlay?,
    setStatus: (EventStatus) -> Unit,
    setRating: (SelfRating) -> Unit,
    setNotes: (String) -> Unit,
    toggleBreak: () -> Unit,
    takeNextSong: () -> Unit,
) {
    Column(1) {
        EventStatusDash(
            startsAt = startsAt,
            endsAt = endsAt,
            breakStartedAt = breakStartedAt,
            status = status,
            setStatus = setStatus,
            toggleBreak = toggleBreak,
        )

        MagicItem(
            song,
            contentAlignment = Alignment.Center,
            isVisibleInit = true,
            offsetX = 50.dp,
            scale = .8f,
        ) { song ->
            if (song != null && songPlay != null) {
                LiveSongDash(
                    title = song.title,
                    notes = songPlay.notes,
                    rating = songPlay.rating,
                    setRating = setRating,
                    setNotes = setNotes,
                    takeNextSong = takeNextSong,
                )
            } else {
                // show something
            }
        }
    }
}

@Composable
@Preview
fun EventLivePreview() {
    val now = Clock.System.now()
    MultiPreview {
        PreviewFrame("Event Dash", "Song staged") {
            LiveEventDash(
                startsAt = now - 10.minutes,
                endsAt = now + 50.minutes,
                breakStartedAt = null,
                status = EventStatus.Live,
                song = mockDb.songs.first(),
                songPlay = NewSongPlay(SongId.random(), null, null),
                setStatus = {},
                setRating = {},
                setNotes = {},
                toggleBreak = {},
                takeNextSong = {},
            )
        }
        PreviewFrame("Event Dash", "Song null") {
            LiveEventDash(
                startsAt = now - 10.minutes,
                endsAt = now + 50.minutes,
                breakStartedAt = null,
                status = EventStatus.Live,
                song = null,
                songPlay = null,
                setStatus = {},
                setRating = {},
                setNotes = {},
                toggleBreak = {},
                takeNextSong = {},
            )
        }
    }
}