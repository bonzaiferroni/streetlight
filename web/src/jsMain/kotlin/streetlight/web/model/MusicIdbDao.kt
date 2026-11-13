package streetlight.web.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.MusicSheet
import streetlight.model.music.Song
import streetlight.model.music.SongId
import streetlight.model.music.SongMusic
import streetlight.model.music.Track
import streetlight.model.studio.MusicDao
import streetlight.web.idb.IdbDb
import streetlight.web.idb.IdbTable

/** The [MusicDao] of a [StudioIdbDb]. */
class MusicIdbDao internal constructor(
    private val db: IdbDb,
    private val songs: IdbTable<Song>,
    private val musics: IdbTable<Music>,
    private val tracks: IdbTable<Track>,
) : MusicDao {
    override fun flowMusics(songId: SongId) = musics.rowsFlow
        .map { rows -> rows.filter { it.songId == songId } }
        .distinctUntilChanged()

    override fun flowMusic(musicId: MusicId) = musics.flowRow(musicId.toString())

    override fun flowSongMusics() = combine(songs.rowsFlow, musics.rowsFlow) { songRows, musicRows ->
        val songsById = songRows.associateBy { it.songId }
        musicRows.mapNotNull { music -> songsById[music.songId]?.let { SongMusic(it, music) } }
    }.distinctUntilChanged()

    override suspend fun saveMusic(music: Music) = musics.save(music)
    override suspend fun removeMusic(musicId: MusicId) = musics.remove(musicId.toString())

    override suspend fun readMusicSheet(musicId: MusicId): MusicSheet? {
        val music = musics.read(musicId.toString()) ?: return null
        return MusicSheet(music, tracks.readRows().filter { it.musicId == musicId })
    }

    override suspend fun saveMusicSheet(sheet: MusicSheet) {
        val musicId = sheet.music.musicId
        val trackIds = sheet.tracks.mapTo(mutableSetOf()) { it.trackId }
        val removedIds = tracks.readRows().filter { it.musicId == musicId && it.trackId !in trackIds }.map { it.trackId }
        db.write {
            save(musics, sheet.music)
            sheet.tracks.forEach { save(tracks, it) }
            removedIds.forEach { remove(tracks, it.toString()) }
        }
    }
}
