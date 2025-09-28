package streetlight.app.ui

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
    val resolution = part.getResolution(notation.measureBeats, notation.measureTime)
    val dividerColor = Pond.localColors.contentDim.copy(.5f)
    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(1) {
            LabeledValue("Key", notationOf(notation.root), modifier = Modifier.padding(horizontal = unitDp))
            LabeledValue(
                "Timing",
                "${notation.measureBeats}/${notation.measureTime}",
                modifier = Modifier.padding(horizontal = unitDp)
            )
        }
        part.sections.forEach { section ->
            val measures = section.getMeasureCount(notation.measureBeats)
            Column(0) {
                Label(section.toLabel())
                val noteCount = section.getNoteCount()
                val groupCount = (noteCount + 3) / 4
                val rowCount = (groupCount + 1) / 2
                repeat(rowCount) { rowIndex ->
                    Row(2) {
                        repeat(2) { rowGroupIndex ->
                            val group = rowIndex * 2 + rowGroupIndex
                            Row(0) {
                                repeat(4) { groupNoteIndex ->
                                    val noteIndex = group * 4 + groupNoteIndex
                                    val note = section.getNote(noteIndex)
                                    val interval = note.interval
                                    if (interval == null) {
                                        Text("-",  modifier = Modifier.width(30.dp))
                                    } else {
                                        Text(
                                            notationOf(interval, notation.root, note.mark),
                                            modifier = Modifier.width(25.dp)
                                        )
                                    }
                                    if (groupNoteIndex != 3) {
                                        Label("|", color = dividerColor)
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
fun SongNotationDashPreview() {
    MultiPreview {
        PreviewFrame("SongNotationDash") {
            SongNotationDash(amazingGrace)
        }
    }
}