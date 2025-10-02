package streetlight.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Trash
import kabinet.utils.removeAt
import kabinet.utils.replaceAt
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Carousel
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.H3
import pondui.ui.controls.H4
import pondui.ui.controls.IconButton
import pondui.ui.controls.LabeledContent
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.SongSection
import streetlight.model.mockDb

@Composable
fun SongPartEditor(
    notation: SongNotation,
    part: SongPart,
    updatePart: (SongPart) -> Unit,
    playChord: (Int, List<Int>) -> Unit,
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
                }
                part.composition.forEachIndexed { index, sectionIndex ->
                    val section = part.sections.getOrNull(sectionIndex)
                    if (section != null) {
                        Section {
                            Row(1) {
                                SongSectionView(notation, part, section, modifier = Modifier.weight(1f))
                                MoreMenu {
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
                H4("Add Section")
                IconButton(TablerIcons.Plus) {
                    updatePart(part.copy(sections = part.sections + SongSection.Empty))
                }
            }

            part.sections.forEachIndexed { sectionIndex, section ->
                Section {
                    SongSectionEditor(notation, part, section, playChord) { modifiedSection ->
                        val sections = if (modifiedSection != null) {
                            part.sections.replaceAt(sectionIndex, section)
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
                playChord = { _, _ -> }
            )
        }
    }
}