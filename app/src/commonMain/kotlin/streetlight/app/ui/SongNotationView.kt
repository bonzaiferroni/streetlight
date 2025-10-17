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
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.modifiers.topBorder
import pondui.ui.services.MidiPlayer
import pondui.ui.services.MiniPlayer
import streetlight.model.data.*
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.app.utils.toMidiSequence

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
                midiPlayer = null,
                capo = capo,
                tempo = tempo
            )
        }
    }
}

@Composable
fun PartSequenceView(
    notation: SongNotation,
    part: SongPart,
    sequence: PartSequence,
    midiPlayer: MidiPlayer?,
    capo: Int?,
    tempo: Int?,
) {
    Row(1) {
        Column(1, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Label(part.instrument.notationLabel)
            when (sequence) {
                is ChordSequence -> ChordSequenceView(
                    notation = notation,
                    part = part,
                    sequence = sequence,
                )
                is VocalSequence -> VocalSequenceView(sequence)
            }
        }
        if (midiPlayer != null) {
            midiPlayer.MiniPlayer {
                sequence.toMidiSequence(
                    beatsPerMeasure = notation.measureBeats,
                    rootPitch = notation.rootPitch,
                    capo = capo,
                    tempo = tempo
                )
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