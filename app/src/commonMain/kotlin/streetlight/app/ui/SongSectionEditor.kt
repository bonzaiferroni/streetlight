package streetlight.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Column
import pondui.ui.controls.Expando
import pondui.ui.controls.H3
import pondui.ui.controls.H4
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.Row
import pondui.ui.controls.TextField
import pondui.ui.services.MidiPlayer
import pondui.ui.services.MiniPlayer
import pondui.ui.services.playChord
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.app.appTheme
import streetlight.app.utils.toMidiSequence
import streetlight.model.data.ChordHelper
import streetlight.model.data.Chromatic
import streetlight.model.data.MeasureChord
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.SongSection
import streetlight.model.data.parseMeasureChord
import streetlight.model.mockDb

@Composable
fun SongSectionEditor(
    notation: SongNotation,
    part: SongPart,
    section: SongSection,
    midiPlayer: MidiPlayer? = null,
    capo: Int? = null,
    tempo: Int? = null,
    modifySection: (SongSection?) -> Unit
) {
    Column(2) {
        Row(1) {
            H3(section.title, modifier = Modifier.weight(1f))
            MoreMenu {
                MoreMenuItem("Delete ${section.title}") {
                    modifySection(null)
                }
            }
        }
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
        Row(1) {
            H4("Chords")
            Expando()
            midiPlayer?.MiniPlayer { section.toMidiSequence(
                beatsPerMeasure = notation.beatsPerMeasure,
                rootPitch = notation.rootPitch,
                capo = capo,
                tempo = tempo
            ) }
        }

        var chordText by remember(part.style) {
            mutableStateOf(buildString {
                section.chords.forEachIndexed { index, chord ->
                    append(chord.toNotation(notation.rootPitch, part.style))
                    if (index < section.chords.size - 1) {
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
                    translation.takeIf { mc ->
                        chords.size == section.chords.size ||
                                chords.size == section.chords.size - 1 &&
                                section.chords[chords.size].expression != mc.expression
                    }?.toNotation()
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
            modifySection(section.copy(chords = chords))
        }

        SongSectionView(notation, part, section)
    }
}

@Preview
@Composable
fun EditSongSectionPreview() {
    MultiPreview(appTheme()) {
        PreviewFrame("EditSongNotation") {
            val notation = mockDb.songs.first().notation!!
            val part = notation.parts.first()
            val section = part.sections.first()
            SongSectionEditor(
                notation = notation,
                part = part,
                section = section,
                modifySection = { },
            )
        }
    }
}