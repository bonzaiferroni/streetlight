package streetlight.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.PlayerStop
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Trash
import kabinet.utils.moveLeft
import kabinet.utils.moveRight
import kabinet.utils.removeAt
import kabinet.utils.replaceAt
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Carousel
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.Expando
import pondui.ui.controls.H3
import pondui.ui.controls.H4
import pondui.ui.controls.IconButton
import pondui.ui.controls.LabeledContent
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.TextField
import pondui.ui.services.MidiPlayer
import pondui.ui.services.MiniPlayer
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.app.utils.toMidiSequence
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.SongSection
import streetlight.model.mockDb

@Composable
fun SongPartEditor(
    notation: SongNotation,
    part: SongPart,
    midiPlayer: MidiPlayer? = null,
    capo: Int? = null,
    tempo: Int? = null,
    updatePart: (SongPart) -> Unit,
) {
    Section {
        Column(2, modifier = Modifier.fillMaxWidth()) {
            H3(part.instrument.label)
            Carousel {
                addItem("instrument/notation") {
                    Row(1) {
                        LabeledContent("instrument", modifier = Modifier.weight(3f)) {
                            DropMenu(part.instrument, { it.label }) { updatePart(part.copy(instrument = it)) }
                        }
                        LabeledContent("notation", modifier = Modifier.weight(2f)) {
                            DropMenu(part.style, { it.label }) { updatePart(part.copy(style = it)) }
                        }
                    }
                }
                addItem("midi", TablerIcons.Settings) {
                    LabeledContent("midi sound") {
                        TextField(
                            text = (part.midiProgram ?: part.instrument.midiProgram).toString(),
                            placeholder = "midi",
                            modifier = Modifier.width(50.dp)
                        ) {
                            val value = it.toIntOrNull() ?: return@TextField
                            updatePart(part.copy(midiProgram = value))
                        }
                    }
                }
            }

            Column(1) {
                Row(1) {
                    H4("Composition")
                    MoreMenu(TablerIcons.Plus) {
                        part.sections.forEachIndexed { index, section ->
                            MoreMenuItem(section.title) {
                                updatePart(part.copy(composition = part.composition + index))
                            }
                        }
                    }
                    Expando()
                    midiPlayer?.MiniPlayer {
                        part.toMidiSequence(
                            beatsPerMeasure = notation.beatsPerMeasure,
                            rootPitch = notation.rootPitch,
                            capo = capo,
                            tempo = tempo
                        )
                    }
                }
                part.composition.forEachIndexed { index, sectionIndex ->
                    val section = part.sections.getOrNull(sectionIndex)
                    if (section != null) {
                        Section {
                            Row(1) {
                                SongSectionView(notation, part, section, modifier = Modifier.weight(1f))
                                MoreMenu {
                                    MoreMenuItem("Move Up", TablerIcons.ArrowUp) {
                                        updatePart(part.copy(composition = part.composition.moveLeft(index)))
                                    }
                                    MoreMenuItem("Move Down", TablerIcons.ArrowDown) {
                                        updatePart(part.copy(composition = part.composition.moveRight(index)))
                                    }
                                    MoreMenuItem("Remove", TablerIcons.Trash) {
                                        updatePart(part.copy(composition = part.composition.removeAt(index)))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(1) {
                H4("Sections")
                IconButton(TablerIcons.Plus) {
                    updatePart(part.copy(sections = part.sections + SongSection.Empty))
                }
            }

            part.sections.forEachIndexed { sectionIndex, section ->
                Section {
                    SongSectionEditor(
                        notation = notation,
                        part = part,
                        section = section,
                        midiPlayer = midiPlayer,
                        capo = capo,
                        tempo = tempo,
                    ) { modifiedSection ->
                        val sections = if (modifiedSection != null) {
                            part.sections.replaceAt(sectionIndex, modifiedSection)
                        } else {
                            part.sections.removeAt(sectionIndex)
                        }
                        updatePart(part.copy(sections = sections))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SongPartEditorPreview() {
    MultiPreview {
        PreviewFrame("EditSongNotation") {
            val notation = mockDb.songs.first().notation!!
            val part = notation.parts.first()
            SongPartEditor(
                notation = notation,
                part = part,
                updatePart = { },
            )
        }
    }
}