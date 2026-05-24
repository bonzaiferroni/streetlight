package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.H1
import pondui.ui.controls.LazyColumnTab
import pondui.ui.controls.TabScaffold
import pondui.ui.controls.Text
import streetlight.app.SongProfileRoute
import streetlight.model.data.SongNotation
import streetlight.model.data.toRecordId

@Composable
fun SongProfileScreen(
    route: SongProfileRoute,
    viewModel: SongProfileModel = viewModel(key = route.id) { SongProfileModel(route.id.toRecordId()) }
) {
    val state by viewModel.stateFlow.collectAsState()

    val song = state.song ?: return

    fun updateNotation(notation: SongNotation?) {
        viewModel.updateSong(song.copy(notation = notation))
    }

    TabScaffold(
        drawerContent = { H1(song.title) }
    ) {
        EditSongTab(
            song = song,
            updateStatus = state.updateStatus,
            updateSong = viewModel::updateSong,
            updateNotation = ::updateNotation,
        )

        LazyColumnTab("Compose", 2) {
            item("sequence header") {
                Text("Sequence goes here")
            }
        }
    }
}