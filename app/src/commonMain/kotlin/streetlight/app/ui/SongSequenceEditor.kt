package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pondui.ui.controls.Column
import pondui.ui.controls.H4
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.modifiers.padTop
import pondui.ui.services.MidiPlayer
import pondui.ui.services.MiniPlayer
import streetlight.app.utils.toMidiSequence
import streetlight.model.data.ChordSequence
import streetlight.model.data.VocalSequence
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.PartSequence

@Composable
fun SongSequenceEditor(
    notation: SongNotation,
    part: SongPart,
    sequence: PartSequence,
    midiPlayer: MidiPlayer?,
    capo: Int?,
    tempo: Int?,
    modifySequence: (PartSequence?) -> Unit,
) {
    Column(2, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(sequence.sequenceId)
        Row(1, modifier = Modifier.padTop(1)) {
            TextField(
                text = sequence.sequenceId,
                label = "sequence id",
                modifier = Modifier.weight(3f)
            ) {
                modifySequence(sequence.setSequenceId(it))
            }
            TextField(
                text = sequence.repetitions.toString(),
                label = "repeats",
                modifier = Modifier.weight(2f)
            ) {
                val repetitions = it.toIntOrNull() ?: sequence.repetitions
                modifySequence(sequence.setRepetitions(repetitions))
            }
            TextField(
                text = sequence.measureBeats?.toString() ?: notation.measureBeats.toString(),
                label = "beats",
                modifier = Modifier.weight(2f)
            ) {
                val measureBeats = it.toIntOrNull()?.takeIf { it != notation.measureBeats }
                modifySequence(sequence.setMeasureBeats(measureBeats))
            }
            MoreMenu {
                MoreMenuItem("Delete ${sequence.sequenceId}") {
                    modifySequence(null)
                }
            }
        }
        Row(1) {
            H4(part.instrument.notationLabel)
            if (midiPlayer != null && sequence is ChordSequence) {
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

        when (sequence) {
            is ChordSequence -> ChordSequenceEditor(
                notation = notation,
                part = part,
                sequence = sequence,
                midiPlayer = midiPlayer,
                modifySection = modifySequence
            )
            is VocalSequence -> VocalSequenceEditor(
                sequence = sequence,
                modifySequence = modifySequence
            )
        }

        PartSequenceView(
            notation = notation,
            part = part,
            sequence = sequence,
        )
    }
}