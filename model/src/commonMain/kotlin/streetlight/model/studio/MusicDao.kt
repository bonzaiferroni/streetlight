package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.MusicSheet
import streetlight.model.music.SongId
import streetlight.model.music.SongMusic

/** The musics of a [StudioDb], and the sheets that join each to its tracks. */
interface MusicDao {
    fun flowMusics(songId: SongId): Flow<List<Music>>
    fun flowMusic(musicId: MusicId): Flow<Music?>

    /** Each music whose song is kept, with its song. */
    fun flowSongMusics(): Flow<List<SongMusic>>
    suspend fun saveMusic(music: Music)

    /** Removes the music [musicId] with its tracks and mixes. */
    suspend fun removeMusic(musicId: MusicId)

    /** The music [musicId] with its tracks, or `null` when there is no such music. */
    suspend fun readMusicSheet(musicId: MusicId): MusicSheet?

    /** Stores the music and tracks of [sheet] together, removing each track of the music the sheet does not hold. */
    suspend fun saveMusicSheet(sheet: MusicSheet)
}
