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
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.app.SongProfileRoute
import streetlight.app.utils.toRoute
import streetlight.model.data.*
import streetlight.model.mockDb

@Composable
fun LiveSongView(
    song: Song,
    notes: String?,
    setNotes: (String) -> Unit,
    takeNextSong: () -> Unit,
) {
    val nav = LocalNav.current
    Column(2, horizontalAlignment = Alignment.CenterHorizontally) {
        Column(0, horizontalAlignment = Alignment.CenterHorizontally) {
            H1(song.title)
            Text(song.artist)
        }
        song.notation?.let {
            SongNotationView(notation = it, capo = song.capo, tempo = song.tempo)
        }
        TextField(
            text = notes,
            onChange = setNotes,
            placeholder = "Notes",
            label = "Notes",
            modifier = Modifier.fillMaxWidth()
        )
        Row(1) {
            Button("edit", color = Pond.colors.secondary) { nav.go(song.songId.toRoute()) }
            Button("Done", onClick = takeNextSong)
        }
    }
}

@Preview
@Composable
fun LiveSongDashPreview() {
    val song = mockDb.songs.first()
    MultiPreview {
        PreviewFrame {
            LiveSongView(
                song,
                notes = "Great song, loved the vibe!",
                setNotes = {},
                takeNextSong = {}
            )
        }
    }
}