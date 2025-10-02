package streetlight.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import kabinet.utils.removeAt
import kabinet.utils.replaceAt
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Column
import pondui.ui.controls.FlowRow
import pondui.ui.controls.H3
import pondui.ui.controls.H4
import pondui.ui.controls.Row
import pondui.ui.controls.TextField
import pondui.ui.modifiers.onHotKeyConsume
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.app.appTheme
import streetlight.model.data.ChordHelper
import streetlight.model.data.Chromatic
import streetlight.model.data.MeasureChord
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.SongSection
import streetlight.model.data.parseMeasureChord
import streetlight.model.mockDb

@Composable
fun EditNotationSection(
    notation: SongNotation,
    part: SongPart,
    section: SongSection,
    playChord: (List<Int>) -> Unit,
    modifySection: (SongSection) -> Unit
) {
    fun modifyChord(chordIndex: Int, chord: MeasureChord) {
        val chords = section.chords.replaceAt(chordIndex, chord)
        modifySection(section.copy(chords = chords))
    }

    Column(2) {
        H3(section.title)
        Row(1) {
            TextField(
                text = section.title,
                placeholder = "name",
                label = "name",
                modifier = Modifier.weight(1f)
            ) {
                modifySection(section.copy(title = it))
            }
            TextField(
                text = section.repetitions.toString(),
                placeholder = "repeats",
                label = "repeats",
                modifier = Modifier.weight(1f)
            ) {
                val repetitions = it.toIntOrNull() ?: section.repetitions
                modifySection(section.copy(repetitions = repetitions))
            }
        }
        H4("Chords")

        var chordText by remember { mutableStateOf(buildString {
            section.chords.forEachIndexed { index, chord ->
                append(chord.toNotation(notation.rootPitch, part.style))
                if (index < section.chords.size - 1) {
                    if (chord.isPhraseEnd)
                        append('\n')
                    else
                        append(' ')
                }
            }
        }) }

        val rootChromatic = Chromatic.ofPitch(notation.rootPitch)

        TextField(
            text = chordText,
            style = Pond.typo.mono,
            modifier = Modifier.fillMaxWidth()
        ) { editedText ->
            chordText = editedText
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
                    translation.takeIf { t ->
                        chords.size == section.chords.size ||
                                chords.size == section.chords.size - 1 &&
                                section.chords[chords.size].expression != t.expression
                    }?.toNotation()
                        ?.let { expressionNotation ->
                            ChordHelper.map[expressionNotation]?.let { midiChord -> playChord(midiChord) }
                        }
                    chords.add(translation)
                }
            }
            modifySection(section.copy(chords = chords))
        }

        SectionChords(section, notation)
    }
}

@Preview
@Composable
fun EditNotationSectionPreview() {
    MultiPreview(appTheme()) {
        PreviewFrame("EditSongNotation") {
            val notation = mockDb.songs.first().notation!!
            val part = notation.parts.first()
            val section = part.sections.first()
            EditNotationSection(
                notation = notation,
                part = part,
                section = section,
                modifySection = { },
                playChord = { }
            )
        }
    }
}