package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Column
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*

@Composable
fun LiveSongView(
    title: String,
    notes: String?,
    rating: SelfRating?,
    setRating: (SelfRating) -> Unit,
    setNotes: (String) -> Unit,
    takeNextSong: () -> Unit,
) {
    val unitDp = Pond.ruler.unitSpacing
    Column(2, horizontalAlignment = Alignment.CenterHorizontally) {
        H1(title)
        FlowRow(1, horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            LabeledValue("Artist", "John Lennon", modifier = Modifier.padding(horizontal = unitDp))
            LabeledValue("Capo", "3rd Fret", modifier = Modifier.padding(horizontal = unitDp))
            LabeledValue("Tempo", "90 bpm", modifier = Modifier.padding(horizontal = unitDp))
        }
//        Section {
//            // SongNotationView()
//        }
        Button("Done", onClick = takeNextSong)
    }
}

@Preview
@Composable
fun LiveSongDashPreview() {
    MultiPreview {
        PreviewFrame {
            LiveSongView(
                title = "Imagine",
                notes = "Great song, loved the vibe!",
                rating = null,
                setRating = {},
                setNotes = {},
                takeNextSong = {}
            )
        }
    }
}