package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*
import streetlight.model.mockDb

@Composable
fun EditSongDash(
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
                text = song.artist ?: "",
                placeholder = "Artist",
                label = "artist",
                modifier = Modifier.weight(1f)
            ) { updateSong(song.copy(artist = it.takeIf { it.isNotBlank() })) }
            Row(1, modifier = Modifier.weight(1f)) {
                TextField(
                    text = song.capo?.toString() ?: "",
                    placeholder = "capo",
                    label = "capo",
                    modifier = Modifier.weight(1f)
                ) { updateSong(song.copy(capo = it.toIntOrNull())) }
                TextField(
                    text = song.tempo?.toString() ?: "",
                    placeholder = "tempo",
                    label = "bpm",
                    modifier = Modifier.weight(1f)
                ) { updateSong(song.copy(tempo = it.toIntOrNull())) }
            }
        }
        EditSongNotation(song.notation) { updateSong(song.copy(notation = it)) }
    }
}

@Preview
@Composable
fun EditSongDashPreview() {
    MultiPreview {
        PreviewFrame("EditSongDash") {
            EditSongDash(
                song = mockDb.songs.first(),
                updateStatus = UpdateStatus.Done,
                updateSong = { }
            )
        }
    }
}
