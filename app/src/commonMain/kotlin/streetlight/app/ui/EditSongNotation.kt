package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Minus
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import kabinet.utils.removeAt
import kabinet.utils.replaceAt
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.ui.modifiers.onHotKeyConsume
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*
import streetlight.model.mockDb
import kotlin.collections.plus

@Composable
fun EditNotationHeader(
    notation: SongNotation,
    updateNotation: (SongNotation?) -> Unit,
) {
    Column(2) {
        Row(1) {
            H2("Notation", modifier = Modifier.weight(1f))
            MoreMenu {
                MoreMenuItem("Remove notation", icon = TablerIcons.Trash) { updateNotation(null) }
            }
        }
        Row(1, modifier = Modifier.fillMaxWidth()) {
            TextField(
                text = notation.rootPitch.toString(),
                placeholder = "root",
                label = "root",
                modifier = Modifier.weight(1f)
            ) { updateNotation(notation.copy(rootPitch = it.toIntOrNull() ?: notation.rootPitch)) }
            TextField(
                text = notation.beatsPerMeasure.toString(),
                placeholder = "beats",
                label = "beats",
                modifier = Modifier.weight(1f)
            ) { updateNotation(notation.copy(beatsPerMeasure = it.toIntOrNull() ?: notation.beatsPerMeasure)) }
            TextField(
                text = notation.beatValue.toString(),
                placeholder = "timing",
                label = "timing",
                modifier = Modifier.weight(1f)
            ) { updateNotation(notation.copy(beatValue = it.toIntOrNull() ?: notation.beatValue)) }
        }
        Row(1) {
            Text("Add Instrument")
            MoreMenu(TablerIcons.Plus, TablerIcons.Minus) {
                MoreMenuItem("Rhythm Guitar") {
                    updateNotation(notation.copy(parts = notation.parts + SongPart(Instrument.RhythmGuitar)))
                }
            }
        }
    }
}

@Composable
fun EditNotationPart(
    notation: SongNotation,
    part: SongPart,
    modifyPart: (SongPart) -> Unit
) {
    Section {
        Column(2, modifier = Modifier.fillMaxWidth()) {
            H3(part.instrument.label)
            DropMenu(part.instrument, { it.label }, label = "instrument") {
                modifyPart(part.copy(instrument = it))
            }
            DropMenu(part.style, { it.label }, label = "notation") {
                modifyPart(part.copy(style = it))
            }
            Row(1) {
                Text("Add Section")
                IconButton(TablerIcons.Plus) {
                    modifyPart(part.copy(sections = part.sections + SongSection.Empty))
                }
            }

            fun modifySection(sectionIndex: Int, section: SongSection) {
                val sections = part.sections.replaceAt(sectionIndex, section)
                modifyPart(part.copy(sections = sections))
            }
            part.sections.forEachIndexed { sectionIndex, section ->
                Section {
                    EditNotationSection(notation, part, section) {
                        modifySection(sectionIndex, it)
                    }
                }
            }
        }
    }
}

@Composable
fun EditNotationSection(
    notation: SongNotation,
    part: SongPart,
    section: SongSection,
    modifySection: (SongSection) -> Unit
) {
    Column(2) {
        H3(section.title)
        Row(1) {
            TextField(
                text = section.title,
                placeholder = "name",
                label = "name",
            ) {
                modifySection(section.copy(title = it))
            }
            TextField(
                text = section.repetitions.toString(),
                placeholder = "repeats",
                label = "repeats",
                modifier = Modifier.width(100.dp)
            ) {
                val repetitions = it.toIntOrNull() ?: section.repetitions
                modifySection(section.copy(repetitions = repetitions))
            }
        }
        H4("Notes")
        FlowRow(1, maxItemsInEachRow = 4) {
            fun modifyChord(chordIndex: Int, chord: MeasureChord) {
                val chords = section.chords.replaceAt(chordIndex, chord)
                modifySection(section.copy(chords = chords))
            }

            val width = 60.dp
            repeat(section.chords.size + 1) { chordIndex ->
                val chord = section.chords.getOrNull(chordIndex)
                val expression = chord?.expression
                var text by remember(expression, notation.rootPitch, part.style) {
                    val text = expression?.let { notationOf(it, notation.rootPitch, part.style) } ?: ""
                    mutableStateOf(text)
                }
                TextField(text,
                    modifier = Modifier.width(width).onHotKeyConsume(Key.Backspace) {
                        if (text.isEmpty() && section.chords.size > chordIndex) {
                            modifySection(section.copy(chords = section.chords.removeAt(chordIndex)))
                            true
                        } else {
                            false
                        }
                    }
                ) {
                    text = it
                    val parsedChord = parseChord(it, part.style)
                    if (chord != null) {
                        modifyChord(chordIndex, chord.copy(expression = parsedChord))
                    } else if (parsedChord != null) {
                        modifySection(section.copy(chords = section.chords + MeasureChord(1, parsedChord)))
                    }
                }
            }
        }
        SongSectionDash(section, notation)
    }
}

@Preview
@Composable
fun EditSongNotationPreview() {
    MultiPreview {
        PreviewFrame("EditSongNotation") {
//            EditSongNotation(
//                notation = mockDb.songs.first().notation,
//                updateNotation = { }
//            )
        }
    }
}