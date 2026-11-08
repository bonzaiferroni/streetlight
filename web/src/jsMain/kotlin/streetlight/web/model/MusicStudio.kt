package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.toDataOr
import kampfire.model.reactIn
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.io.MixDb
import streetlight.model.music.Chroma
import streetlight.model.music.Key
import streetlight.model.music.Mode
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.Song
import streetlight.model.music.SongId
import streetlight.model.music.TimeSignature
import streetlight.model.music.text.NotationStyle
import streetlight.model.music.text.SongMarkdown
import streetlight.web.utility.localStoreOf

/** The songs held in [db], their mixes in [mixDb], and the editors of them. */
class MusicStudio(
    private val scope: CoroutineScope,
    private val db: SongDb,
    private val mixDb: MixDb,
) {
    val songList = db.songsState.tapOf { songs -> songs.map { it.song } }

    /** The style every song is written in. */
    val notationStyleState = localStoreOf(scope, NOTATION_STYLE_KEY, NotationStyle.Letters)

    /** The parts of each track's notation its editor shows. */
    val notationViewState = localStoreOf(scope, NOTATION_VIEW_KEY, NotationView.Both)

    /** Stores a new song titled [title], returning its id. */
    fun create(title: String): SongId {
        val song = newStoredSong(title)
        db.save(song)
        return song.song.songId
    }

    /** Removes the song of [songId] and the mixes of its music. Their clips stay in the user's library. */
    fun remove(songId: SongId) {
        val musicId = db.read(songId)?.music?.musicId
        db.remove(songId)
        if (musicId == null) return
        scope.launch {
            val mixes = mixDb.readMixes(musicId).toDataOr { return@launch }
            mixes.forEach { mixDb.removeMix(it.mixId) }
        }
    }

    fun readSong(songId: SongId) = db.read(songId)

    /**
     * Stores a new song read from the markdown [text], titled for [fileName] when it names no title, returning its id,
     * or gives the problem that kept it from being read.
     */
    fun importSong(text: String, fileName: String): Outcome<SongId> {
        val blank = newStoredSong(fileName.substringBeforeLast('.'))
        val imported = SongMarkdown.read(text, blank.song, blank.music, notationStyleState.now).toDataOr { return it }

        db.save(StoredSong(imported.song, imported.music, imported.tracks))
        return Ok(imported.song.songId)
    }

    /** An editor of the song of [songId] for as long as [scope] lasts, saving its changes, or `null` with no such song. */
    fun editorOf(scope: CoroutineScope, songId: SongId): MusicEditor? {
        val song = db.read(songId) ?: return null
        val editor = MusicEditor(scope, song, notationStyleState, mixDb)
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
private const val NOTATION_VIEW_KEY = "streetlight.notation-view"
