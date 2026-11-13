package streetlight.web.model

import streetlight.model.music.Song
import streetlight.model.music.SongId
import streetlight.model.studio.SongDao
import streetlight.web.idb.IdbTable

/** The [SongDao] of a [StudioIdbDb]. */
class SongIdbDao internal constructor(
    private val songs: IdbTable<Song>,
) : SongDao {
    override fun flowSongs() = songs.rowsFlow
    override fun flowSong(songId: SongId) = songs.flowRow(songId.toString())
    override suspend fun saveSong(song: Song) = songs.save(song)
    override suspend fun removeSong(songId: SongId) = songs.remove(songId.toString())
}
