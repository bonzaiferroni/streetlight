package streetlight.app.ui

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import kabinet.utils.removeAt
import kabinet.utils.replaceAt
import pondui.ui.controls.Column
import pondui.ui.controls.FlowRow
import pondui.ui.controls.H3
import pondui.ui.controls.H4
import pondui.ui.controls.Row
import pondui.ui.controls.Text
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
    fun modifyChord(chordIndex: Int, chord: MeasureChord) {
        val chords = section.chords.replaceAt(chordIndex, chord)
        modifySection(section.copy(chords = chords))
    }

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
        }
        H4("Chords")

        Column(1) {
            val width = 60.dp
            val chordCount = section.chords.size + 1
            var measureBeats = 0
            var measureCount = 0

            val phraseIndices = section.chords.mapIndexedNotNull { index, mc ->
                if (mc.isPhraseEnd) index + 1 else null
            } + chordCount
            phraseIndices.forEachIndexed { index, phraseEndIndex ->
                FlowRow(1) {
                    val phraseStart = index.takeIf { it > 0 }?.let { phraseIndices[it - 1] } ?: 0
                    val phraseLength = phraseEndIndex - phraseStart
                    repeat(phraseLength) { phraseIndex ->
                        val chordIndex = phraseStart + phraseIndex
                        if (measureBeats == 0 || measureBeats >= notation.beatsPerMeasure) {
                            BarLine(measureCount % 4 == 0)
                            measureCount++
                            measureBeats = 0
                        }
                        val measureChord = section.chords.getOrNull(chordIndex)
                        measureBeats += measureChord?.duration ?: (notation.beatsPerMeasure - measureBeats)
                        val expressionText = measureChord?.toNotation(notation.rootPitch, part.style)
                        var text by remember { mutableStateOf(expressionText ?: "") }
                        LaunchedEffect(expressionText) {
                            text = expressionText ?: text
                        }
                        TextField(
                            text,
                            minWidth = width,
                            modifier = Modifier.onHotKeyConsume(Key.Backspace) {
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
                                translation?.takeIf { it != measureChord?.expression }?.toNotation()
                                    ?.let { expressionNotation ->
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
                    BarLine(true)
                }
            }
        }

        SectionChords(section, notation)
    }
}