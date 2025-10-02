package streetlight.app.ui

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
        part.composition.forEach { sectionIndex ->
            val section = part.sections.getOrNull(sectionIndex) ?: return@forEach
            SectionChords(
                notation = notation,
                part = part,
                section = section,
            )
        }
    }
}

@Composable
fun SectionChords(
    notation: SongNotation,
    part: SongPart,
    section: SongSection,
) {
    Column(1) {
        Label(section.toLabel())
        var chordIndex = 0
        var measureBeats = 0
        var measureCount = 0
        val phraseIndices = section.chords.mapIndexedNotNull {
            index, mc -> if (mc.isPhraseEnd || index + 1 == section.chords.size) index + 1 else null
        }
        phraseIndices.forEach { phraseIndex ->
            FlowRow(1) {
                while (chordIndex < phraseIndex) {
                    val measureChord = section.chords[chordIndex++]
                    if (measureBeats == 0 || measureBeats >= notation.beatsPerMeasure) {
                        BarLine(measureCount % 4 == 0)
                        measureCount++
                        measureBeats = 0
                    }
                    measureBeats += measureChord.duration ?: (notation.beatsPerMeasure - measureBeats)
                    Text(
                        measureChord.expression?.let { notationOf(it, notation.rootPitch, part.style) } ?: "-",
                        modifier = Modifier.widthIn(min = 25.dp)
                    )
                }
                BarLine(true)
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