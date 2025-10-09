package streetlight.app.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Instant
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.ui.services.MediaEvent
import pondui.ui.services.MediaEventEffect
import streetlight.model.data.*

@Composable
fun LiveEventView(
    eventId: EventId,
    startsAt: Instant,
    endsAt: Instant,
    status: EventStatus,
    setStatus: (EventStatus) -> Unit,
    viewModel: LiveEventModel = viewModel { LiveEventModel(eventId) }
) {
    val state by viewModel.stateFlow.collectAsState()

    MediaEventEffect {
        if (state.isActive) {
            when (it) {
                MediaEvent.PlayPause,
                MediaEvent.Next -> viewModel.takeNextSong()
                else -> { }
            }
        }
    }

    Column(1, modifier = Modifier.verticalScroll(rememberScrollState())) {
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
        ) { eventSong ->
            val song = eventSong?.song
            val request = eventSong?.request
            val songPlay = state.songPlay
            if (song != null && songPlay != null) {
                TabSection {
                    Tab("Song") {
                        request?.let { request ->
                            H2("Requested by ${request.requesterName ?: "anonymous"}")
                            Text("Is joining? ${request.isJoining}")
                            Text("Comment: ${request.comment}")
                        }
                        LiveSongView(
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
                Button("Next Song", onClick = viewModel::takeNextSong)
            }
        }
    }
}