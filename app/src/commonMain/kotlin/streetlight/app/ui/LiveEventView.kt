package streetlight.app.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.ui.services.MediaEvent
import pondui.ui.services.MediaEventEffect
import pondui.ui.theme.Pond
import streetlight.model.data.*

@Composable
fun LiveEventView(
    event: Event,
    modifyEvent: (Event) -> Unit,
    viewModel: LiveEventModel = viewModel (key = event.eventId.value + "live") { LiveEventModel(event.eventId) }
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

    LaunchedEffect(Unit) {
        viewModel.refreshSong()
    }

    Column(
        gap = 2,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        EventStatusDash(
            event = event,
            breakStartedAt = state.breakStartedAt,
            setStatus = { status ->
                if (state.song == null && status == EventStatus.Live)
                    viewModel.takeNextSong()
                modifyEvent(event.copy(status = status))
            },
            toggleBreak = viewModel::toggleBreak,
        )

        Column(1) {
            LabeledValue("Announcer:", state.announcerStatus)
            Row(2) {
                LabeledContent("Outro:") {
                    Checkbox(state.announceOutro, onChange = viewModel::toggleOutro)
                }
                LabeledContent("Interlude:") {
                    Checkbox(state.announceInterlude, onChange = viewModel::toggleInterlude)
                }
                LabeledContent("Intro:") {
                    Checkbox(state.announceIntro, onChange = viewModel::toggleIntro)
                }
            }
        }

        MagicItem(
            state.song,
            contentAlignment = Alignment.Center,
            isVisibleInit = true,
            offsetX = 50.dp,
            scale = .8f,
        ) { eventSong ->
            val song = eventSong?.song
            val request = eventSong?.request
            val rendition = state.rendition
            if (song != null && rendition != null) {
                TabSection(tabColor = Pond.colors.selection.copy(.5f)) {
                    Tab("Song") {
                        request?.let { request ->
                            H2("Requested by ${request.requesterName ?: "anonymous"}")
                            Text("Is joining? ${request.isJoining}")
                            Text("Comment: ${request.comment}")
                        }
                        LiveSongView(
                            song = song,
                            notes = rendition.notes,
                            setNotes = viewModel::setNotes,
                            takeNextSong = viewModel::takeNextSong,
                        )
                    }
                    val notation = song.notation
                    val vocalPart = notation?.parts?.firstOrNull { it.instrument == Instrument.Vocals }
                    if (vocalPart != null) {
                        Tab("Lyrics") {
                            LyricsView(notation, vocalPart, song.capo, song.tempo)
                        }
                    }
                }
            } else {
                Button("Next Song", onClick = viewModel::takeNextSong)
            }
        }
    }
}