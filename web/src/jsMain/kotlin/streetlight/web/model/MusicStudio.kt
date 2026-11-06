package streetlight.web.model

import kampfire.model.reactIn
import kampfire.model.storeOf
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

/** The songs held in [db] and the [MusicStudioState.focus] the studio shows in place of them. */
class MusicStudio(
    scope: CoroutineScope,
    private val db: SongDb,
) {
    private val state = storeOf(MusicStudioState())

    val focusState = state.tapOf { it.focus }
    val songList = db.songsState.tapOf { songs -> songs.map { it.song } }

    /** The style every song is written in. */
    val notationStyleState = localStoreOf(scope, NOTATION_STYLE_KEY, NotationStyle.Letters)

    fun load(songId: SongId) = state.set { copy(focus = songId) }

    /** Stores a new song titled [title] and focuses it. */
    fun create(title: String) {
        val song = newStoredSong(title)
        db.save(song)
        load(song.song.songId)
    }

    fun cancelFocus() = state.set { copy(focus = null) }

    fun remove(songId: SongId) = db.remove(songId)

    /** An editor of the song of [songId] for as long as [scope] lasts, saving its changes, or `null` with no such song. */
    fun editorOf(scope: CoroutineScope, songId: SongId): MusicEditor? {
        val song = db.read(songId) ?: return null
        val editor = MusicEditor(scope, song, notationStyleState)
        editor.storedState.reactIn(scope) { db.save(it) }
        return editor
    }
}

/** The state of the studio, with [focus] the song it shows, or `null` to list the songs. */
data class MusicStudioState(
    val focus: SongId? = null,
)

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
