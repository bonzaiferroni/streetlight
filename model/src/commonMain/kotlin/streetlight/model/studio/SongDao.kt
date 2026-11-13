package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.Song
import streetlight.model.music.SongId

/** The songs of a [StudioDb]. */
interface SongDao {
    fun flowSongs(): Flow<List<Song>>
    fun flowSong(songId: SongId): Flow<Song?>
    suspend fun saveSong(song: Song)

    /** Removes the song [songId] and each music of it. */
    suspend fun removeSong(songId: SongId)
}
