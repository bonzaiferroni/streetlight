package streetlight.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Music
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Settings
import kabinet.utils.replaceAt
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Carousel
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.H3
import pondui.ui.controls.IconButton
import pondui.ui.controls.Label
import pondui.ui.controls.LabeledContent
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
fun EditSongPart(
    notation: SongNotation,
    part: SongPart,
    updatePart: (SongPart) -> Unit,
    playChord: (Int, List<Int>) -> Unit,
) {
    Section {
        Column(2, modifier = Modifier.fillMaxWidth()) {
            H3(part.instrument.label)
            Carousel {
                addItem {
                    Row(1) {
                        LabeledContent("instrument", modifier = Modifier.weight(3f)) {
                            DropMenu(part.instrument, { it.label }) { updatePart(part.copy(instrument = it)) }
                        }
                        LabeledContent("notation", modifier = Modifier.weight(2f)) {
                            DropMenu(part.style, { it.label }) { updatePart(part.copy(style = it)) }
                        }
                    }
                }
                addItem(TablerIcons.Settings) {
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
            Row(1) {
                Text("Add Section")
                IconButton(TablerIcons.Plus) {
                    updatePart(part.copy(sections = part.sections + SongSection.Empty))
                }
            }

            fun updateSection(sectionIndex: Int, section: SongSection) {
                val sections = part.sections.replaceAt(sectionIndex, section)
                updatePart(part.copy(sections = sections))
            }
            part.sections.forEachIndexed { sectionIndex, section ->
                Section {
                    EditSongSection(notation, part, section, playChord) {
                        updateSection(sectionIndex, it)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun EditNotationPartPreview() {
    MultiPreview {
        PreviewFrame("EditSongNotation") {
            val notation = mockDb.songs.first().notation!!
            val part = notation.parts.first()
            EditSongPart(
                notation = notation,
                part = part,
                updatePart = { },
                playChord = { _, _ -> }
            )
        }
    }
}