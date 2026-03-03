package streetlight.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pondui.ui.controls.Button
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.actionable
import pondui.ui.theme.Pond
import streetlight.app.utils.toRoute

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
@Composable
fun SongFeedScreen(
    viewModel: SongFeedModel = viewModel { SongFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            FlowRow(1) {
                TextField(
                    state.newName,
                    onChange = viewModel::setNewName,
                    placeholder = "Name",
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    state.newArtist,
                    onChange = viewModel::setNewArtist,
                    placeholder = "Artist",
                    modifier = Modifier.weight(1f)
                )
                Button("Create", onClick = viewModel::createItem, modifier = Modifier.fillMaxRowHeight())
            }
        }
        LazyColumn {
            items(state.songs) { song ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                        .actionable(song.songId.toRoute())
                        .padding(vertical = Pond.ruler.unitSpacing)
                ) {
                    Text(song.title)
                }
            }
        }

        LottieSpirit("happy-fox")
    }
}