package streetlight.app.ui

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Column
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Label
import pondui.ui.controls.LabeledValue
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.theme.Pond
import streetlight.model.data.*
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame

@Composable
fun SongNotationDash(
    notation: SongNotation
) {
    val unitDp = Pond.ruler.unitSpacing
    val instrument = Instrument.RhythmGuitar
    val part = notation.parts.first { it.instrument == instrument }
    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(1) {
            val rootChromatic = Chromatic.ofPitch(notation.rootPitch)
            LabeledValue("Key", rootChromatic.label, modifier = Modifier.padding(horizontal = unitDp))
            LabeledValue(
                "Timing",
                "${notation.beatsPerMeasure}/${notation.beatValue}",
                modifier = Modifier.padding(horizontal = unitDp)
            )
        }
        part.sections.forEach { section ->
            SectionChords(
                section = section,
                notation = notation,
            )
        }
    }
}

@Composable
fun SectionChords(
    section: SongSection,
    notation: SongNotation,
) {
    val dividerColor = Pond.localColors.contentDim.copy(.5f)
    Column(0) {
        Label(section.toLabel())
        val noteCount = section.chords.size
        val groupCount = (noteCount + 3) / 4
        val rowCount = (groupCount + 1) / 2
        repeat(rowCount) { rowIndex ->
            Row(1) {
                repeat(2) { rowGroupIndex ->
                    val group = rowIndex * 2 + rowGroupIndex
                    Row(1, modifier = Modifier.height(IntrinsicSize.Max)) {
                        repeat(4) { groupNoteIndex ->
                            val noteIndex = group * 4 + groupNoteIndex
                            if (noteIndex % section.beatResolution == 0) {
                                BarLine()
                            }
                            val chord = noteIndex.takeIf { it < section.chords.size}?.let { section.getChordAt(it) }
                            val expression = chord?.expression
                            if (expression != null) {
                                Text(
                                    notationOf(expression, notation.rootPitch),
                                    modifier = Modifier.width(25.dp)
                                )
                            } else {
                                Text("-", modifier = Modifier.width(25.dp))
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
fun SongNotationDashPreview() {
    MultiPreview {
        PreviewFrame("SongNotationDash") {
            SongNotationDash(amazingGrace)
        }
    }
}