package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Star
import pondui.ui.controls.Column
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.ui.theme.Pond
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
    Section {
        Column(2, horizontalAlignment = Alignment.CenterHorizontally) {
            H1(title)
            SelfRatingScale(
                rating = rating,
                setRating = setRating,
            )
            Column(1) {
                TextField(
                    notes ?: "",
                    placeholder = "song notes",
                    label = "notes",
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