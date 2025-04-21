package streetlight.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Controls
import pondui.ui.controls.Text
import pondui.ui.controls.TextButton
import pondui.ui.controls.TextField
import pondui.ui.nav.Scaffold
import streetlight.app.SongListRoute

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SongListScreen(
    route: SongListRoute,
    viewModel: SongListModel = viewModel { SongListModel(route) }
) {
    val state by viewModel.state.collectAsState()

    Scaffold {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Controls {
                TextField(state.newName, viewModel::setNewName, "Name", modifier = Modifier.weight(1f))
                TextField(state.newArtist, viewModel::setNewArtist, "Artist", modifier = Modifier.weight(1f))
                TextButton("Create", onClick = viewModel::createItem, modifier = Modifier.fillMaxRowHeight())
            }
        }
        LazyColumn {
            items(state.songs) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(it.name)
                    TextButton("👉", onClick = { })
                }
            }
        }
    }
}