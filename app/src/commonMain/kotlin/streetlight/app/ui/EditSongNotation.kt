package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Minus
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import kabinet.utils.replaceAt
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*
import streetlight.model.mockDb

@Composable
fun EditSongNotation(
    notation: SongNotation?,
    updateNotation: (SongNotation?) -> Unit,
) {
    MagicItem(notation, offsetX = 50.dp, scale = .8f) { notation ->
        if (notation == null) {
            Button("Add notation", color = Pond.colors.primary) {
                updateNotation(SongNotation.Empty)
            }
            return@MagicItem
        }

        fun addPart(part: SongPart) {
            updateNotation(notation.copy(parts = notation.parts + part))
        }
        fun modifyPart(partIndex: Int, part: SongPart) {
            updateNotation(notation.copy(parts = notation.parts.replaceAt(partIndex, part)))
        }

        Column(2) {
            Row(1) {
                H2("Notation", modifier = Modifier.weight(1f))
                MoreMenu {
                    MoreMenuItem("Remove notation", icon = TablerIcons.Trash) { updateNotation(null) }
                }
            }
            Row(1, modifier = Modifier.fillMaxWidth()) {
                TextField(
                    text = notation.root.toString(),
                    placeholder = "root",
                    label = "root",
                    modifier = Modifier.weight(1f)
                ) { updateNotation(notation.copy(root = it.toIntOrNull() ?: notation.root)) }
                TextField(
                    text = notation.measureBeats.toString(),
                    placeholder = "beats",
                    label = "beats",
                    modifier = Modifier.weight(1f)
                ) { updateNotation(notation.copy(measureBeats = it.toIntOrNull() ?: notation.measureBeats)) }
                TextField(
                    text = notation.measureTime.toString(),
                    placeholder = "timing",
                    label = "timing",
                    modifier = Modifier.weight(1f)
                ) { updateNotation(notation.copy(measureTime = it.toIntOrNull() ?: notation.measureTime)) }
            }
            Row(1) {
                Text("Add Instrument")
                MoreMenu(TablerIcons.Plus, TablerIcons.Minus) {
                    MoreMenuItem("Rhythm Guitar") { addPart(SongPart(Instrument.RhythmGuitar)) }
                }
            }
            LazyColumn(1) {
                itemsIndexed(notation.parts) { partIndex, part ->
                    Section {
                        Column(2, modifier = Modifier.fillMaxWidth()) {
                            H3(part.instrument.label)
                            DropMenu(part.instrument, label = "instrument") {
                                modifyPart(partIndex, part.copy(instrument = it))
                            }
                            DropMenu(part.style, label = "notation") {
                                modifyPart(partIndex, part.copy(style = it))
                            }
                            Row(1) {
                                Text("Add Section")
                                IconButton(TablerIcons.Plus) {
                                    modifyPart(partIndex, part.copy(sections = part.sections + SongSection.Empty))
                                }
                            }

                            fun modifySection(sectionIndex: Int, section: SongSection) {
                                val sections = part.sections.replaceAt(sectionIndex, section)
                                modifyPart(partIndex, part.copy(sections = sections))
                            }
                            part.sections.forEachIndexed { sectionIndex, section ->
                                Section {
                                    Column(2) {
                                        H3(section.title)
                                        Row(1) {
                                            TextField(
                                                text = section.title,
                                                placeholder = "name",
                                                label = "name",
                                            ) {
                                                modifySection(sectionIndex, section.copy(title = it))
                                            }
                                            TextField(
                                                text = section.repetitions.toString(),
                                                placeholder = "repeats",
                                                label = "repeats",
                                                modifier = Modifier.width(100.dp)
                                            ) {
                                                val repetitions = it.toIntOrNull() ?: section.repetitions
                                                modifySection(sectionIndex, section.copy(repetitions = repetitions))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun EditSongNotationPreview() {
    MultiPreview {
        PreviewFrame("EditSongNotation") {
            EditSongNotation(
                notation = mockDb.songs.first().notation,
                updateNotation = { }
            )
        }
    }
}
