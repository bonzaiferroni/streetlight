package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import kabinet.utils.moveLeft
import kabinet.utils.moveRight
import kabinet.utils.removeAt
import pondui.ui.controls.Column
import pondui.ui.controls.Expando
import pondui.ui.controls.H4
import pondui.ui.controls.IconButton
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.services.MiniPlayer
import streetlight.app.utils.toMidiSequence
import streetlight.model.data.SongNotation
import streetlight.model.data.SongSection

@Composable
fun CompositionEditor(
    notation: SongNotation,
    modifyNotation: (SongNotation) -> Unit
) {
    Column(1) {
        Row(1) {
            H4("Composition")
            IconButton(TablerIcons.Plus) {
                val sectionId = notation.suggestSectionId("Verse")
                val section = SongSection(sectionId = sectionId, measures = 0, layers = emptyList())
                modifyNotation(notation.copy(composition = notation.composition + section))
            }
            Expando()
//            midiPlayer?.MiniPlayer {
//                part.toMidiSequence(
//                    beatsPerMeasure = notation.measureBeats,
//                    rootPitch = notation.rootPitch,
//                    capo = capo,
//                    tempo = tempo
//                )
//            }
        }
        notation.composition.forEachIndexed { index, section ->
//            Section {
//                Row(1) {
//                    ChordSequenceView(notation, part, section, modifier = Modifier.weight(1f))
//                    MoreMenu {
//                        MoreMenuItem("Move Up", TablerIcons.ArrowUp) {
//                            updatePart(part.copy(composition = part.composition.moveLeft(index)))
//                        }
//                        MoreMenuItem("Move Down", TablerIcons.ArrowDown) {
//                            updatePart(part.copy(composition = part.composition.moveRight(index)))
//                        }
//                        MoreMenuItem("Remove", TablerIcons.Trash) {
//                            updatePart(part.copy(composition = part.composition.removeAt(index)))
//                        }
//                    }
//                }
//            }
        }
    }
}