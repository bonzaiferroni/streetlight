package streetlight.app.ui

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Star
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*
import streetlight.model.mockDb

@Composable
fun EventLiveDash(
    status: EventStatus,
    setStatus: (EventStatus) -> Unit,
    viewModel: EventLiveModel = viewModel { EventLiveModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    EventDash(
        status = status,
        song = state.song,
        setStatus = setStatus,
        takeNextSong = viewModel::takeNextSong
    )
}

@Composable
fun EventDash(
    status: EventStatus,
    song: Song?,
    setStatus: (EventStatus) -> Unit,
    takeNextSong: () -> Unit,
) {
    Column(1) {
//        EventStatusDash(
//            status = status,
//            setStatus = setStatus,
//        )

        MagicItem(song, contentAlignment = Alignment.Center, isVisibleInit = true) { song ->
            if (song != null) {
                SongDash(song)
            } else {
                Button("Take next song", onClick = takeNextSong)
            }
        }
    }
}

@Composable
fun SongDash(
    song: Song,
) {
    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
        H2(song.title)
        SelfRatingScale()
    }
}

@Composable
fun SelfRatingScale() {
    Row(1,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.height(IntrinsicSize.Max)
    ) {
        SelfRating.entries.forEach { rating ->
            Column(
                gap = 1,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
                    .actionable { }
            ) {
                Icon(TablerIcons.Star)
                Label(
                    rating.label,
                    modifyStyle = { it.copy(textAlign = TextAlign.Center) },
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
@Preview
fun EventLivePreview() {
    MultiPreview {
        PreviewFrame("Event Dash", "Song staged") {
            EventDash(
                status = EventStatus.Live,
                song = mockDb.songs.first(),
                setStatus = {},
                takeNextSong = {}
            )
        }
        PreviewFrame("Event Dash", "Song null") {
            EventDash(
                status = EventStatus.Live,
                song = null,
                setStatus = {},
                takeNextSong = {}
            )
        }
    }
}