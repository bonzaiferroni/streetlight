package streetlight.app.ui

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.replaceAt
import pondui.ui.controls.H1
import pondui.ui.controls.LazyTab
import pondui.ui.controls.TabScaffold
import pondui.ui.services.playChord
import pondui.ui.services.rememberMidiPlayer
import streetlight.app.SongProfileRoute
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.toProjectId

@Composable
fun SongProfileScreen(
    route: SongProfileRoute,
    viewModel: SongProfileModel = viewModel (key = route.id) { SongProfileModel(route.id.toProjectId()) }
) {
    val state by viewModel.stateFlow.collectAsState()

    val song = state.song ?: return

    fun updateNotation(notation: SongNotation?) {
        viewModel.updateSong(song.copy(notation = notation))
    }

    val midi = rememberMidiPlayer()

    TabScaffold(
        drawerContent = { H1(song.title) }
    ) {
        LazyTab("Edit", 2) {
            item("edit song header") {
                EditSongHeader(
                    song = song,
                    updateStatus = state.updateStatus,
                    updateSong = viewModel::updateSong
                )
            }

            song.notation?.let { notation ->
                item("edit notation header") {
                    EditNotationHeader(notation, ::updateNotation)
                }

                itemsIndexed(notation.parts) { partIndex, part ->
                    fun modifyPart(part: SongPart) {
                        updateNotation(notation.copy(parts = notation.parts.replaceAt(partIndex, part)))
                    }

                    EditSongPart(notation, part, ::modifyPart) { program, chords ->
                        midi.playChord(chords, program = program)
                    }
                }
            }
        }
    }
}