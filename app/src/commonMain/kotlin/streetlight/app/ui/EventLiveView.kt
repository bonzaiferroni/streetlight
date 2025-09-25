package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerTrackNext
import kabinet.utils.toTimeDescription
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.InProgressIndicator
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import streetlight.model.data.Event

@Composable
fun EventLiveView(
    event: Event,
    viewModel: EventLiveModel = viewModel(key = event.eventId.value) { EventLiveModel(event) }
) {
    val state by viewModel.stateFlow.collectAsState()

    Column(1) {
        Row(1) {
            Text("Updated at ${event.createdAt.toTimeDescription()}")
            InProgressIndicator(state.updateStatus)
        }
        DropMenu(
            event.status,
            label = "status",
            onChange = viewModel::setStatus,
        )
        Button(TablerIcons.PlayerTrackNext, onClick = viewModel::takeNextSong)
        state.song?.let { song ->
            Text(song.title)
        }
    }
}
