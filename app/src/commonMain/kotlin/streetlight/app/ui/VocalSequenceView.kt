package streetlight.app.ui

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import streetlight.model.data.VocalSequence
import streetlight.model.data.notationOf

@Composable
fun VocalSequenceView(
    sequence: VocalSequence,
) {
    val text = remember(sequence.notes) {
        buildString {
            var lastUtterance: String? = null
            sequence.notes.forEach { note ->
                var utterance = note.utterance ?: return@forEach
                val isContinued = utterance.endsWith('-')
                if (isContinued) {
                    utterance = utterance.take(utterance.length - 1)
                }
                if (utterance.isEmpty() || utterance.matchesEnd(lastUtterance)) return@forEach
                append(utterance)
                if (!isContinued && !note.isPhraseEnd)
                    append(' ')
                if (note.isPhraseEnd)
                    append('\n')
            }
        }
    }
    Text(text)
}

private fun String.matchesEnd(other: String?): Boolean {
    if (other == null) return false
    val n = this.length
    val m = other.length
    if (n > m) return false
    val start = m - n
    var i = 0
    while (i < n) {
        if (this[i] != other[start + i]) return false
        i++
    }
    return true
}