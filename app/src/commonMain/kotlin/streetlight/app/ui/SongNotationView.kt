package streetlight.app.ui

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
import pondui.ui.controls.Text
import pondui.ui.modifiers.topBorder
import streetlight.model.data.*
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame

@Composable
fun SongNotationView(
    notation: SongNotation,
    capo: Int? = null,
    tempo: Int? = null,
) {
    val instrument = Instrument.RhythmGuitar
    val part = notation.parts.first { it.instrument == instrument }
    Column(2, horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(2, verticalGap = 1) {
            val rootChromatic = Chromatic.ofPitch(notation.rootPitch)
            LabeledValue("Key:", rootChromatic.label)
            LabeledValue("Timing:", "${notation.measureBeats}/${notation.beatValue}",)
            capo?.let {
                LabeledValue("Capo:", it)
            }
            tempo?.let {
                LabeledValue("Tempo:", it)
            }
        }
        part.sequences.forEach { sequence ->
            PartSequenceView(
                notation = notation,
                part = part,
                sequence = sequence,
            )
        }
    }
}

@Composable
fun PartSequenceView(
    notation: SongNotation,
    part: SongPart,
    sequence: PartSequence,
) {
    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
        Label(sequence.toLabel())
        when (sequence) {
            is ChordSequence -> ChordSequenceView(
                notation = notation,
                part = part,
                sequence = sequence,
            )
            is VocalSequence -> Text(sequence.lyrics)
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