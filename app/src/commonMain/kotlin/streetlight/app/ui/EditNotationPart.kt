package streetlight.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import kabinet.utils.replaceAt
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.H3
import pondui.ui.controls.IconButton
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.SongSection
import streetlight.model.mockDb

@Composable
fun EditNotationPart(
    notation: SongNotation,
    part: SongPart,
    updatePart: (SongPart) -> Unit,
    playChord: (List<Int>) -> Unit,
) {
    Section {
        Column(2, modifier = Modifier.fillMaxWidth()) {
            H3(part.instrument.label)
            DropMenu(part.instrument, { it.label }, label = "instrument") {
                updatePart(part.copy(instrument = it))
            }
            DropMenu(part.style, { it.label }, label = "notation") {
                updatePart(part.copy(style = it))
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
                    EditNotationSection(notation, part, section, playChord) {
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
            EditNotationPart(
                notation = notation,
                part = part,
                updatePart = { },
                playChord = { }
            )
        }
    }
}