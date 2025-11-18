package streetlight.app.ui

import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.Minus
import compose.icons.tablericons.Plus
import kabinet.utils.replaceOrRemoveAt
import pondui.ui.controls.LazyColumnTab
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.TabContentScope
import pondui.ui.controls.TabItem
import pondui.ui.controls.Tabs
import pondui.ui.controls.UpdateStatus
import pondui.ui.services.rememberMidiPlayer
import pondui.ui.theme.Pond
import pondui.utils.mixWith
import streetlight.model.data.Instrument
import streetlight.model.data.Song
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart

@Composable
fun TabContentScope.EditSongTab(
    song: Song,
    updateStatus: UpdateStatus,
    updateSong: (Song) -> Unit,
    updateNotation: (SongNotation?) -> Unit,
) {
    LazyColumnTab("Edit", 2) {
        item("edit song header") {
            EditSongHeader(
                song = song,
                updateStatus = updateStatus,
                updateSong = updateSong
            )
        }

        song.notation?.let { notation ->
            item("edit notation header") {
                SongNotationEditor(notation, updateNotation)
            }

            item("edit instruments") {
                val midi = rememberMidiPlayer()
                Tabs(
                    items = notation.parts,
                    tabColor = Pond.colors.selection.mixWith(Pond.colors.primary).copy(alpha = 0.5f),
                    headerContent = {
                        MoreMenu(TablerIcons.Plus, TablerIcons.Minus) {
                            Instrument.entries.forEach { instrument ->
                                if (notation.parts.any { it.instrument == instrument }) return@forEach
                                MoreMenuItem(instrument.label) {
                                    updateNotation(notation.copy(parts = notation.parts + SongPart.createEmpty(instrument)))
                                }
                            }
                        }
                    }
                ) { partIndex, part ->
                    TabItem(part.instrument.label) {
                        SongPartEditor(
                            notation = notation,
                            part = part,
                            midiPlayer = midi,
                            capo = song.capo,
                            tempo = song.tempo,
                        ) { updateNotation(notation.copy(parts = notation.parts.replaceOrRemoveAt(partIndex, it))) }
                    }
                }
            }
        }
    }
}