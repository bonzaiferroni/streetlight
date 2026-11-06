package streetlight.web.model

import kampfire.model.reactIn
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Chroma
import streetlight.model.data.Key
import streetlight.model.data.Mode
import streetlight.model.data.Music
import streetlight.model.data.MusicId
import streetlight.model.data.NotationStyle
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.data.TimeSignature
import streetlight.web.utility.localStoreOf

/** The songs held in [db], and the editors of them. */
class MusicStudio(
    scope: CoroutineScope,
    private val db: SongDb,
) {
    val songList = db.songsState.tapOf { songs -> songs.map { it.song } }

    /** The style every song is written in. */
    val notationStyleState = localStoreOf(scope, NOTATION_STYLE_KEY, NotationStyle.Letters)

    /** Stores a new song titled [title], returning its id. */
    fun create(title: String): SongId {
        val song = newStoredSong(title)
        db.save(song)
        return song.song.songId
    }

    fun remove(songId: SongId) = db.remove(songId)

    /** An editor of the song of [songId] for as long as [scope] lasts, saving its changes, or `null` with no such song. */
    fun editorOf(scope: CoroutineScope, songId: SongId): MusicEditor? {
        val song = db.read(songId) ?: return null
        val editor = MusicEditor(scope, song, notationStyleState)
        editor.storedState.reactIn(scope) { db.save(it) }
        return editor
    }
}

private fun newStoredSong(title: String): StoredSong {
    val songId = SongId.random()
    return StoredSong(
        song = Song(songId, title, ""),
        music = Music(
            musicId = MusicId.random(),
            songId = songId,
            key = Key(Chroma.of(0), Mode.Major),
            time = TimeSignature(4, 4),
        ),
    )
}

private const val NOTATION_STYLE_KEY = "streetlight.notation-style"
