package streetlight.app.ui

import androidx.compose.runtime.Composable
import pondui.ui.controls.Column
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.VocalSequence

@Composable
fun LyricsView(
    notation: SongNotation,
    part: SongPart,
    capo: Int?,
    tempo: Int?,
) {
    Column(1) {
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