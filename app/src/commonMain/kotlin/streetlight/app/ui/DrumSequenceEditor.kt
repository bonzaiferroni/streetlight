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
import pondui.ui.theme.Pond
import streetlight.model.data.DrumSequence
import streetlight.model.data.DrumSound
import streetlight.model.data.PartSequence
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.parseDrumSound

@Composable
fun DrumSequenceEditor(
    part: SongPart,
    sequence: DrumSequence,
    midiPlayer: MidiPlayer? = null,
    modifySequence: (PartSequence?) -> Unit
) {
    var editText by remember(part.style, sequence.sequenceId) {
        mutableStateOf(buildString {
            sequence.sounds.forEachIndexed { index, note ->
                append(note.toNotation())
                if (index < sequence.sounds.size - 1) {
                    if (note.isPhraseEnd)
                        append('\n')
                    else
                        append(' ')
                }
            }
        })
    }

    TextField(
        text = editText,
        style = Pond.typo.mono.copy(fontSize = 12.sp),
        modifier = Modifier.fillMaxWidth()
    ) { editedText ->
        editText = editedText
        val sounds = mutableListOf<DrumSound>()
        val phraseTexts = editedText.split('\n')
        phraseTexts.forEach { phraseText ->
            val noteTexts = phraseText.split(' ')
            noteTexts.forEachIndexed { index, noteText ->
                if (noteText.isEmpty()) return@forEachIndexed
                val isPhraseEnd = index == noteTexts.size - 1
                val drumSound = parseDrumSound(noteText, isPhraseEnd) ?: return@forEachIndexed

                val isNewLastNote = sounds.size == sequence.sounds.size ||
                        sounds.size == sequence.sounds.size - 1
//                translation.pitch?.takeIf { isNewLastNote }?.let { pitch ->
//                    midiPlayer?.playNote(
//                        note = pitch,
//                        program = part.midiProgram ?: part.instrument.midiProgram
//                    )
//                }
                sounds.add(drumSound)
            }
        }
        modifySequence(sequence.copy(sounds = sounds))
    }
}