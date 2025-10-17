package streetlight.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.TextField
import pondui.ui.services.MidiPlayer
import pondui.ui.services.playChord
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.app.appTheme
import streetlight.model.data.ChordHelper
import streetlight.model.data.ChordSequence
import streetlight.model.data.Chromatic
import streetlight.model.data.MeasureChord
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.PartSequence
import streetlight.model.data.parseMeasureChord
import streetlight.model.mockDb

@Composable
fun ChordSequenceEditor(
    notation: SongNotation,
    part: SongPart,
    sequence: ChordSequence,
    midiPlayer: MidiPlayer? = null,
    modifySequence: (PartSequence?) -> Unit
) {
    var editText by remember(part.style, sequence.sequenceId) {
        mutableStateOf(buildString {
            sequence.chords.forEachIndexed { index, chord ->
                append(chord.toNotation(notation.rootPitch, part.style))
                if (index < sequence.chords.size - 1) {
                    if (chord.isPhraseEnd)
                        append('\n')
                    else
                        append(' ')
                }
            }
        })
    }

    val rootChromatic = Chromatic.ofPitch(notation.rootPitch)

    TextField(
        text = editText,
        style = Pond.typo.mono,
        modifier = Modifier.fillMaxWidth()
    ) { editedText ->
        editText = editedText
        val chords = mutableListOf<MeasureChord>()
        val phraseTexts = editedText.split('\n')
        phraseTexts.forEach { phraseText ->
            val chordTexts = phraseText.split(' ')
            chordTexts.forEachIndexed { index, chordText ->
                if (chordText.isEmpty()) return@forEachIndexed
                val isPhraseEnd = index == chordTexts.size - 1
                val measureChord = parseMeasureChord(chordText, part.style, isPhraseEnd) ?: return@forEachIndexed
                val translation = measureChord.expression?.let { e ->
                    measureChord.copy(
                        expression = e.copy(
                            pitch = e.pitch - rootChromatic.pitch,
                            slash = e.slash?.let { s -> s - rootChromatic.pitch }
                        )
                    )
                } ?: measureChord
                val isNewLastChord = chords.size == sequence.chords.size ||
                        chords.size == sequence.chords.size - 1 &&
                        sequence.chords[chords.size].expression != translation.expression
                translation.takeIf { isNewLastChord }?.toNotation()
                    ?.let { expressionNotation ->
                        ChordHelper.map[expressionNotation]?.let { midiChord ->
                            midiPlayer?.playChord(
                                notes = midiChord,
                                program = part.midiProgram ?: part.instrument.midiProgram
                            )
                        }
                    }
                chords.add(translation)
            }
        }
        modifySequence(sequence.copy(chords = chords))
    }
}

@Preview
@Composable
fun EditSongSectionPreview() {
    MultiPreview(appTheme()) {
        PreviewFrame("EditSongNotation") {
            val notation = mockDb.songs.first().notation!!
            val part = notation.parts.first()
            val section = part.sequences.first() as ChordSequence
            ChordSequenceEditor(
                notation = notation,
                part = part,
                sequence = section,
                modifySequence = { },
            )
        }
    }
}