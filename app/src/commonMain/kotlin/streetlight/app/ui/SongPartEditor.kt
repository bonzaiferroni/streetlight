package streetlight.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Settings
import kabinet.utils.replaceOrRemoveAt
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Button
import pondui.ui.controls.Carousel
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.Icon
import pondui.ui.controls.LabeledContent
import pondui.ui.controls.ReorderableAccordion
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Switch
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.modifiers.pad
import pondui.ui.services.MidiPlayer
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.SongNotation
import streetlight.model.data.SongPart
import streetlight.model.data.suggestVariation
import streetlight.model.mockDb

@Composable
fun SongPartEditor(
    notation: SongNotation,
    part: SongPart,
    midiPlayer: MidiPlayer? = null,
    capo: Int? = null,
    tempo: Int? = null,
    modifyPart: (SongPart?) -> Unit,
) {
    var isReorderable by remember { mutableStateOf(false) }

    Section {
        Column(1, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Carousel {
                addItem("instrument/notation") {
                    Row(1) {
//                        LabeledContent("instrument", modifier = Modifier.weight(3f)) {
//                            DropMenu(part.instrument, { it.label }) { modifyPart(part.copy(instrument = it)) }
//                        }
                        LabeledContent("notation", modifier = Modifier.weight(2f)) {
                            DropMenu(part.style, { it.label }) { modifyPart(part.copy(style = it)) }
                        }
                        Switch(
                            isOn = isReorderable,
                            text = "Reorder",
                            color = Pond.colors.secondary,
                            shape = Pond.ruler.torpedo
                        ) { isReorderable = it }
                    }
                }
                addItem("midi", TablerIcons.Settings) {
                    Row(1) {
                        LabeledContent("midi sound") {
                            TextField(
                                text = (part.midiProgram ?: part.instrument.midiProgram).toString(),
                                placeholder = "midi",
                                modifier = Modifier.width(50.dp)
                            ) {
                                val value = it.toIntOrNull() ?: return@TextField
                                modifyPart(part.copy(midiProgram = value))
                            }
                        }
                        Button("Remove", color = Pond.colors.negation) {
                            modifyPart(null)
                        }
                    }
                }
            }

            ReorderableAccordion(
                items = part.sequences,
                isReorderable = isReorderable,
                provideHeader = { Text(it.sequenceId)},
                onChange = { modifyPart(part.copy(sequences = it)) }
            ) { sequenceIndex, sequence ->
                SongSequenceEditor(
                    notation = notation,
                    part = part,
                    sequence = sequence,
                    midiPlayer = midiPlayer,
                    capo = capo,
                    tempo = tempo,
                ) { modifiedSequence ->
                    val sequences = part.sequences.replaceOrRemoveAt(sequenceIndex, modifiedSequence)
                    modifyPart(part.copy(sequences = sequences))
                }
            }

            Row(
                gap = 1,
                modifier = Modifier.clip(Pond.ruler.unitCorners)
                    .clickable {
                        val sequenceId = part.sequences.suggestVariation("Verse") { it.sequenceId }
                        modifyPart(part.copy(sequences = part.sequences + part.instrument.createSequence(sequenceId)))
                    }
                    .pad(1)
            ) {
                Text("Add sequence", color = Pond.localColors.contentDim)
                Icon(TablerIcons.Plus)
            }
        }
    }
}

@Preview
@Composable
fun SongPartEditorPreview() {
    MultiPreview {
        PreviewFrame("EditSongNotation") {
            val notation = mockDb.songs.first().notation!!
            val part = notation.parts.first()
            SongPartEditor(
                notation = notation,
                part = part,
                modifyPart = { },
            )
        }
    }
}