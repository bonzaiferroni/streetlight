package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.MusicId
import streetlight.model.music.Track
import streetlight.model.music.TrackId

/** The tracks of a [StudioDb]. */
interface TrackDao {
    fun flowTracks(musicId: MusicId): Flow<List<Track>>
    fun flowTrack(trackId: TrackId): Flow<Track?>
    suspend fun saveTrack(track: Track)

    /** Removes the track [trackId] with its section clips and studies. */
    suspend fun removeTrack(trackId: TrackId)
}
