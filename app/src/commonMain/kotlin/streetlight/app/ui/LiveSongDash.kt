package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Column
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*

@Composable
fun LiveSongDash(
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
        Section {
            // SongNotationView()
        }
        Section {
            Column(2) {
                SelfRatingScale(
                    rating = rating,
                    setRating = setRating,
                )
                Column(1) {
                    TextField(
                        notes ?: "",
                        placeholder = "song notes",
                        label = "notes to self",
                        onChange = setNotes,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MagicItem(rating != null, offsetX = 5.dp) { isRated ->
                            if (isRated) {
                                Button("Done", onClick = takeNextSong)
                            } else {
                                TextButton("Skip", onClick = takeNextSong)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LiveSongDashPreview() {
    MultiPreview {
        PreviewFrame {
            LiveSongDash(
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