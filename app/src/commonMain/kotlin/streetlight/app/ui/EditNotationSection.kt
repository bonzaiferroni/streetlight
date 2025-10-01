package streetlight.app.ui

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import kabinet.utils.removeAt
import kabinet.utils.replaceAt
import pondui.ui.controls.Column
import pondui.ui.controls.H3
import pondui.ui.controls.H4
import pondui.ui.controls.Row
import pondui.ui.controls.TextField
import pondui.ui.modifiers.onHotKeyConsume
import streetlight.model.data.ChordHelper
import streetlight.model.data.Chromatic
import streetlight.model.data.MeasureChord
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.SongSection
import streetlight.model.data.notationOf
import streetlight.model.data.parseMeasureChord

@Composable
fun EditNotationSection(
    notation: SongNotation,
    part: SongPart,
    section: SongSection,
    playChord: (List<Int>) -> Unit,
    modifySection: (SongSection) -> Unit
) {
    Column(2) {
        H3(section.title)
        Row(1) {
            TextField(
                text = section.title,
                placeholder = "name",
                label = "name",
                modifier = Modifier.weight(1f)
            ) {
                modifySection(section.copy(title = it))
            }
            TextField(
                text = section.repetitions.toString(),
                placeholder = "repeats",
                label = "repeats",
                modifier = Modifier.weight(1f)
            ) {
                val repetitions = it.toIntOrNull() ?: section.repetitions
                modifySection(section.copy(repetitions = repetitions))
            }
            TextField(
                text = section.beatResolution.toString(),
                placeholder = "timing",
                label = "timing",
                modifier = Modifier.weight(1f)
            ) {
                val resolution = it.toIntOrNull() ?: section.beatResolution
                modifySection(section.copy(beatResolution = resolution))
            }
        }
        H4("Notes")
        Column(1) {
            fun modifyChord(chordIndex: Int, chord: MeasureChord) {
                val chords = section.chords.replaceAt(chordIndex, chord)
                modifySection(section.copy(chords = chords))
            }

            val width = 60.dp
            val chordCount = section.chords.size + 1
            val rowCount = chordCount / 4 + 1

            repeat(rowCount) { rowIndex ->
                val columnCount = minOf(4, chordCount - rowIndex * 4)

                Row(1, modifier = Modifier.height(IntrinsicSize.Max)) {
                    repeat(columnCount) { columnIndex ->
                        val chordIndex = rowIndex * 4 + columnIndex
                        if (chordIndex % section.beatResolution == 0) {
                            BarLine()
                        }
                        val measureChord = section.chords.getOrNull(chordIndex)
                        val expressionText =
                            measureChord?.expression?.let { notationOf(it, notation.rootPitch, part.style) }
                        var text by remember { mutableStateOf("") }
                        LaunchedEffect(expressionText) {
                            text = expressionText ?: text
                        }
                        TextField(
                            text,
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
                            val parsedMeasureChord = parseMeasureChord(it, part.style)?.let { c ->
                                val rootChromatic = Chromatic.ofPitch(notation.rootPitch)
                                val translation = c.expression?.let { e ->
                                    e.copy(
                                        pitch = e.pitch - rootChromatic.pitch,
                                        slash = e.slash?.let { s -> s - rootChromatic.pitch }
                                    )
                                }
                                translation?.takeIf { it != measureChord?.expression }?.toNotation()?.let { expressionNotation ->
                                    ChordHelper.map[expressionNotation]?.let { midiChord -> playChord(midiChord) }
                                }
                                c.copy(expression = translation)
                            }
                            if (parsedMeasureChord != null) {
                                if (measureChord != null) {
                                    modifyChord(chordIndex, parsedMeasureChord)
                                } else {
                                    modifySection(section.copy(chords = section.chords + parsedMeasureChord))
                                }
                            }
                        }
                    }
                }
            }
        }
        SectionChords(section, notation)
    }
}