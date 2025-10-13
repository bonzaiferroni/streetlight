package streetlight.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Column
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Label
import pondui.ui.controls.LabeledValue
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.modifiers.topBorder
import pondui.ui.theme.Pond
import streetlight.model.data.*
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame

@Composable
fun SongNotationView(
    notation: SongNotation,
    capo: Int? = null,
    tempo: Int? = null,
    showComposition: Boolean = true,
) {
    val instrument = Instrument.RhythmGuitar
    val part = notation.parts.first { it.instrument == instrument }
    Column(2, horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(2, verticalGap = 1) {
            val rootChromatic = Chromatic.ofPitch(notation.rootPitch)
            LabeledValue("Key:", rootChromatic.label)
            LabeledValue("Timing:", "${notation.beatsPerMeasure}/${notation.beatValue}",)
            capo?.let {
                LabeledValue("Capo:", it)
            }
            tempo?.let {
                LabeledValue("Tempo:", it)
            }
        }
        val partIndices = if (showComposition) part.composition else part.sections.mapIndexed { index, _ -> index }
        partIndices.forEach { sectionIndex ->
            val section = part.sections.getOrNull(sectionIndex) ?: return@forEach
            SongSectionView(
                notation = notation,
                part = part,
                section = section,
            )
        }
    }
}

@Composable
fun SongSectionView(
    notation: SongNotation,
    part: SongPart,
    section: SongSection,
    modifier: Modifier = Modifier,
) {
    val borderColor = Color(0xFF808080)
    Column(1, horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Label(section.toLabel())
        var chordIndex = 0
        var measureBeats = 0
        val phraseIndices = section.chords.mapIndexedNotNull { index, mc ->
            if (mc.isPhraseEnd || index + 1 == section.chords.size) index + 1 else null
        }
        phraseIndices.forEach { phraseIndex ->
            FlowRow(1, modifier = Modifier.topBorder(borderColor)) {
                BarLine(true, color = borderColor)
                while (chordIndex < phraseIndex) {
                    val measureChord = section.chords[chordIndex++]
                    measureBeats += measureChord.duration ?: notation.beatsPerMeasure
                    Text(
                        measureChord.expression?.let { notationOf(it, notation.rootPitch, part.style) } ?: "-",
                        modifier = Modifier.widthIn(min = 25.dp)
                    )
                    if (measureBeats % notation.beatsPerMeasure == 0) {
                        BarLine(measureChord.isPhraseEnd, doubleBar = measureChord.duration == 8, color = borderColor)
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
        PreviewFrame("SongNotationView") {
            SongNotationView(amazingGrace)
        }
    }
}