package streetlight.app.ui

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Text
import pondui.ui.modifiers.topBorder
import streetlight.app.ui.BarLine
import streetlight.model.data.ChordSequence
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.notationOf


@Composable
fun ChordSequenceView(
    notation: SongNotation,
    part: SongPart,
    sequence: ChordSequence,
) {
    val borderColor = Color(0xFF808080)
    var chordIndex = 0
    var measureBeats = 0
    val phraseIndices = sequence.chords.mapIndexedNotNull { index, mc ->
        if (mc.isPhraseEnd || index + 1 == sequence.chords.size) index + 1 else null
    }
    phraseIndices.forEach { phraseIndex ->
        FlowRow(1, modifier = Modifier.topBorder(borderColor)) {
            BarLine(true, color = borderColor)
            while (chordIndex < phraseIndex) {
                val measureChord = sequence.chords[chordIndex++]
                measureBeats += measureChord.duration ?: notation.measureBeats
                Text(
                    measureChord.expression?.let { notationOf(it, notation.rootPitch, part.style) } ?: "-",
                    modifier = Modifier.widthIn(min = 25.dp)
                )
                if (measureBeats % notation.measureBeats == 0) {
                    BarLine(measureChord.isPhraseEnd, doubleBar = measureChord.duration == 8, color = borderColor)
                }
            }
        }
    }
}