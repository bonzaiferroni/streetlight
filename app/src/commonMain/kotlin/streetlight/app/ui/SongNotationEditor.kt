package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import compose.icons.TablerIcons
import compose.icons.tablericons.Minus
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*
import streetlight.model.mockDb
import kotlin.collections.plus

@Composable
fun SongNotationEditor(
    notation: SongNotation,
    updateNotation: (SongNotation?) -> Unit,
) {
    Column(2) {
        Row(1) {
            H2("Notation", modifier = Modifier.weight(1f))
            MoreMenu {
                MoreMenuItem("Remove notation", icon = TablerIcons.Trash) { updateNotation(null) }
            }
        }
        Row(1, modifier = Modifier.fillMaxWidth()) {
            TextField(
                text = notation.rootPitch.toString(),
                placeholder = "root",
                label = "root",
                modifier = Modifier.weight(1f)
            ) { updateNotation(notation.copy(rootPitch = it.toIntOrNull() ?: notation.rootPitch)) }
            TextField(
                text = notation.beatsPerMeasure.toString(),
                placeholder = "beats",
                label = "beats",
                modifier = Modifier.weight(1f)
            ) { updateNotation(notation.copy(beatsPerMeasure = it.toIntOrNull() ?: notation.beatsPerMeasure)) }
            TextField(
                text = notation.beatValue.toString(),
                placeholder = "timing",
                label = "timing",
                modifier = Modifier.weight(1f)
            ) { updateNotation(notation.copy(beatValue = it.toIntOrNull() ?: notation.beatValue)) }
        }
        Row(1) {
            H4("Instruments")
            MoreMenu(TablerIcons.Plus, TablerIcons.Minus) {
                MoreMenuItem("Rhythm Guitar") {
                    updateNotation(notation.copy(parts = notation.parts + SongPart.Empty.copy(instrument = Instrument.RhythmGuitar)))
                }
            }
        }
    }
}

@Preview
@Composable
fun EditNotationHeaderPreview() {
    MultiPreview {
        PreviewFrame("EditSongNotation") {
            SongNotationEditor(
                notation = mockDb.songs.first().notation!!,
                updateNotation = { }
            )
        }
    }
}