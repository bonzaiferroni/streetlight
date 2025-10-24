package streetlight.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import pondui.ui.controls.TextField
import pondui.ui.services.MidiPlayer
import pondui.ui.services.playChord
import pondui.ui.services.playNote
import pondui.ui.theme.Pond
import streetlight.model.data.ChordHelper
import streetlight.model.data.Chromatic
import streetlight.model.data.PartSequence
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.VocalNote
import streetlight.model.data.VocalSequence
import streetlight.model.data.parseVocalNote

@Composable
fun VocalSequenceEditor(
    notation: SongNotation,
    part: SongPart,
    sequence: VocalSequence,
    midiPlayer: MidiPlayer? = null,
    modifySequence: (PartSequence?) -> Unit
) {
    var editText by remember(part.style, sequence.sequenceId) {
        mutableStateOf(buildString {
            sequence.notes.forEachIndexed { index, note ->
                append(note.toNotation(notation.rootPitch, part.style))
                if (index < sequence.notes.size - 1) {
                    if (note.isPhraseEnd)
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
        style = Pond.typo.mono.copy(fontSize = 12.sp),
        modifier = Modifier.fillMaxWidth()
    ) { editedText ->
        editText = editedText
        val notes = mutableListOf<VocalNote>()
        val phraseTexts = editedText.split('\n')
        phraseTexts.forEach { phraseText ->
            val noteTexts = phraseText.split(' ')
            noteTexts.forEachIndexed { index, noteText ->
                if (noteText.isEmpty()) return@forEachIndexed
                val isPhraseEnd = index == noteTexts.size - 1
                val vocalNote = parseVocalNote(noteText, part.style, isPhraseEnd) ?: return@forEachIndexed
                val translation = vocalNote.pitch?.let { pitch ->
                    vocalNote.copy(pitch = pitch + rootChromatic.pitch)
                } ?: vocalNote
                val isNewLastNote = notes.size == sequence.notes.size ||
                        notes.size == sequence.notes.size - 1 &&
                        sequence.notes[notes.size].pitch != translation.pitch
                translation.pitch?.takeIf { isNewLastNote }?.let { pitch ->
                    midiPlayer?.playNote(
                        note = pitch,
                        program = part.midiProgram ?: part.instrument.midiProgram
                    )
                }
                notes.add(vocalNote)
            }
        }
        modifySequence(sequence.copy(notes = notes))
    }
}