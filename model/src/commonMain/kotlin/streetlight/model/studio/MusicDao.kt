package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.MusicSheet
import streetlight.model.music.SongId

/** The musics of a [StudioDb], and the sheets that join each to its song and tracks. */
interface MusicDao {
    fun flowMusics(songId: SongId): Flow<List<Music>>
    fun flowMusic(musicId: MusicId): Flow<Music?>
    suspend fun saveMusic(music: Music)

    /** Removes the music [musicId] with its tracks and mixes. */
    suspend fun removeMusic(musicId: MusicId)

    /** The music [musicId] with its song and tracks, or `null` when either the music or its song is missing. */
    suspend fun readMusicSheet(musicId: MusicId): MusicSheet?

    /** Stores the song, music and tracks of [sheet] together. A track the sheet does not hold is left as it is. */
    suspend fun saveMusicSheet(sheet: MusicSheet)
}
