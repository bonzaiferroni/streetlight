package streetlight.app.ui

import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerTrackNext
import compose.icons.tablericons.Star
import kabinet.utils.toTimeDescription
import pondui.ui.controls.*
import streetlight.model.data.*

@Composable
fun EventLiveView(
    core: EventProfileModel,
    viewModel: EventLiveModel = viewModel { EventLiveModel(core) }
) {
    val coreState by core.stateFlow.collectAsState()
    val state by viewModel.stateFlow.collectAsState()

    val event = coreState.event ?: return

    Column(1) {
        Row(1) {
            Text("Updated at ${event.createdAt.toTimeDescription()}")
            InProgressIndicator(coreState.updateStatus)
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
        Row(1, verticalAlignment = Alignment.Top) {
            SelfRating.entries.forEach { rating ->
                Column(
                    gap = 1,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(TablerIcons.Star)
                    Text(rating.label, modifier = Modifier.background(Color.Blue))
                }
            }
        }
    }
}
