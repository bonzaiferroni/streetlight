package streetlight.web.model

import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import streetlight.model.music.Music
import streetlight.model.music.SectionClip
import streetlight.model.music.Song
import streetlight.model.music.SongId
import streetlight.model.music.Track
import streetlight.web.utility.localStoreOf

/** The songs kept in local storage while the song model is shaped in the editor. */
class SongDb(
    scope: CoroutineScope,
) {
    private val state = localStoreOf(scope, SONG_DB_KEY, SongDbState())
    val songsState = state.tapOf { it.songs }

    fun read(songId: SongId) = state.now.songs.firstOrNull { it.song.songId == songId }

    /** Stores [song] in place of the stored song of the same id, or at the end when there is none. */
    fun save(song: StoredSong) = state.update { db ->
        val index = db.songs.indexOfFirst { it.song.songId == song.song.songId }
        val songs = if (index < 0) db.songs + song else db.songs.toMutableList().apply { set(index, song) }
        db.copy(songs = songs)
    }

    fun remove(songId: SongId) = state.update { db ->
        db.copy(songs = db.songs.filterNot { it.song.songId == songId })
    }
}

@Serializable
data class SongDbState(
    val songs: List<StoredSong> = emptyList(),
)

/** A song as it is kept, with its music and tracks, and the [clips] recorded of their sections. */
@Serializable
data class StoredSong(
    val song: Song,
    val music: Music,
    val tracks: List<Track> = emptyList(),
    val clips: List<SectionClip> = emptyList(),
)

private const val SONG_DB_KEY = "streetlight.song-db"
