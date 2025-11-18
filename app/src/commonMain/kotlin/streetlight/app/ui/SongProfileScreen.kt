package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Minus
import compose.icons.tablericons.Plus
import kabinet.utils.replaceAt
import kabinet.utils.replaceOrRemoveAt
import pondui.ui.controls.H1
import pondui.ui.controls.LazyColumnTab
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.TabItem
import pondui.ui.controls.TabScaffold
import pondui.ui.controls.Tabs
import pondui.ui.controls.Text
import pondui.ui.services.rememberMidiPlayer
import pondui.ui.theme.Pond
import pondui.utils.mixWith
import streetlight.app.SongProfileRoute
import streetlight.model.data.Instrument
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.toProjectId

@Composable
fun SongProfileScreen(
    route: SongProfileRoute,
    viewModel: SongProfileModel = viewModel(key = route.id) { SongProfileModel(route.id.toProjectId()) }
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