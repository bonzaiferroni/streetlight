package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.H1
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Tab
import pondui.ui.controls.TabScaffold
import streetlight.app.SongProfileRoute
import streetlight.model.data.toProjectId

@Composable
fun SongProfileScreen(
    route: SongProfileRoute,
    viewModel: SongProfileModel = viewModel (key = route.id) { SongProfileModel(route.id.toProjectId()) }
) {
    val state by viewModel.stateFlow.collectAsState()

    val song = state.song ?: return

    TabScaffold(
        drawerContent = { H1(song.title) }
    ) {
        Tab("Edit") {
            EditSongDash(
                song = song,
                updateStatus = state.updateStatus,
                updateSong = viewModel::updateSong
            )
        }
    }
}