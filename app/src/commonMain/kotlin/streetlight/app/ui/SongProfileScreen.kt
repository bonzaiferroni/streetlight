package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Minus
import compose.icons.tablericons.Plus
import kabinet.utils.replaceAt
import pondui.ui.controls.H1
import pondui.ui.controls.LazyColumnTab
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.TabScaffold
import pondui.ui.controls.Tabs
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

    val midi = rememberMidiPlayer()

    TabScaffold(
        drawerContent = { H1(song.title) }
    ) {
        LazyColumnTab("Edit", 2) {
            item("edit song header") {
                EditSongHeader(
                    song = song,
                    updateStatus = state.updateStatus,
                    updateSong = viewModel::updateSong
                )
            }

            song.notation?.let { notation ->
                item("edit notation header") {
                    SongNotationEditor(notation, ::updateNotation)
                }

                item("edit instruments") {
                    Tabs(
                        tabColor = Pond.colors.selection.mixWith(Pond.colors.primary).copy(alpha = 0.5f),
                        headerContent = {
                            MoreMenu(TablerIcons.Plus, TablerIcons.Minus) {
                                Instrument.entries.forEach {
                                    MoreMenuItem(it.label) {
                                        updateNotation(notation.copy(parts = notation.parts + SongPart.createEmpty(it)))
                                    }
                                }
                            }
                        }
                    ) {
                        notation.parts.forEachIndexed { partIndex, part ->
                            Tab(part.instrument.label) {
                                SongPartEditor(
                                    notation = notation,
                                    part = part,
                                    midiPlayer = midi,
                                    capo = song.capo,
                                    tempo = song.tempo,
                                ) { updateNotation(notation.copy(parts = notation.parts.replaceAt(partIndex, it))) }
                            }
                        }
                    }
                }
            }
        }
    }
}