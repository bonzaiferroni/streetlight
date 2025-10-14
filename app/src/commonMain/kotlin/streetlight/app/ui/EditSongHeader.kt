package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*
import streetlight.model.mockDb

@Composable
fun EditSongHeader(
    song: Song,
    updateStatus: UpdateStatus,
    updateSong: (Song) -> Unit,
) {
    Column(2) {
        UpdateIndicator(
            updatedAt = song.updatedAt,
            updateStatus = updateStatus,
        )
        TextField(
            text = song.title,
            placeholder = "Song title",
            label = "title",
            modifier = Modifier.fillMaxWidth()
        ) { updateSong(song.copy(title = it)) }
        Row(1) {
            TextField(
                text = song.artist,
                placeholder = "Artist",
                label = "artist",
                modifier = Modifier.weight(1f)
            ) { updateSong(song.copy(artist = it)) }
            Row(1, modifier = Modifier.weight(1f)) {
                TextField(
                    text = song.capo?.toString() ?: "",
                    placeholder = "capo",
                    label = "fret",
                    modifier = Modifier.weight(1f)
                ) { updateSong(song.copy(capo = it.toIntOrNull())) }
                TextField(
                    text = song.tempo?.toString() ?: "",
                    placeholder = "tempo",
                    label = "bpm",
                    modifier = Modifier.weight(1f)
                ) { updateSong(song.copy(tempo = it.toIntOrNull())) }
                Checkbox(song.inRotation, "In Rotation", labelPosition = LabelPosition.Top) { updateSong(song.copy(inRotation = it)) }
            }
        }
        if (song.notation == null) {
            Button("Add notation", color = Pond.colors.primary) {
                updateSong(song.copy(notation = SongNotation.Empty))
            }
        }
    }
}

@Preview
@Composable
fun EditSongDashPreview() {
    MultiPreview {
        PreviewFrame("EditSongHeader") {
            EditSongHeader(
                song = mockDb.songs.first(),
                updateStatus = UpdateStatus.Done,
                updateSong = { }
            )
        }
    }
}
