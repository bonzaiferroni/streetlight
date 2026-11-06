package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
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

/** The songs held in [db] and the [MusicEditor] of the one open, whose changes it saves there. */
class MusicStudio(
    private val scope: CoroutineScope,
    private val db: SongDb,
) {
    private val openEditor = storeOf<MusicEditor?>(null)
    private var editorJob: Job? = null

    val songList = db.songsState.tapOf { songs -> songs.map { it.song } }
    val editorState: Tap<MusicEditor?> = openEditor

    /** The style every song is written in. */
    val notationStyleState = localStoreOf(scope, NOTATION_STYLE_KEY, NotationStyle.Letters)

    /** Opens the song of [songId], leaving it as it is when already open. */
    fun load(songId: SongId) {
        if (openEditor.now?.song?.songId == songId) return
        db.read(songId)?.let { open(it) }
    }

    /** Stores a new song titled [title] and opens it. */
    fun create(title: String) {
        val song = newStoredSong(title)
        db.save(song)
        open(song)
    }

    /** Removes the song of [songId], closing it if open. */
    fun remove(songId: SongId) {
        // closing first, or the open editor's last save would store the song again
        if (openEditor.now?.song?.songId == songId) close()
        db.remove(songId)
    }

    private fun open(song: StoredSong) {
        close()
        val job = SupervisorJob(scope.coroutineContext[Job])
        val editorScope = scope + job
        val editor = MusicEditor(editorScope, song, notationStyleState)
        editor.storedState.reactIn(editorScope) { db.save(it) }
        editorJob = job
        openEditor.set(editor)
    }

    private fun close() {
        editorJob?.cancel()
        editorJob = null
        openEditor.set(null)
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
