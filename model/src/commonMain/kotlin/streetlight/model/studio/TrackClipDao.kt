package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.MusicId
import streetlight.model.music.NotationId
import streetlight.model.music.TrackClip
import streetlight.model.music.TrackId

/** The clips bound to tracks and notations in a [StudioDb]. */
interface TrackClipDao {
    fun flowTrackClips(musicId: MusicId): Flow<List<TrackClip>>

    /** Binds the clip of [trackClip] to its track or notation, in place of any clip bound there. */
    suspend fun saveTrackClip(trackClip: TrackClip)

    /** Unbinds the clip of the notation [notationId] of the track [trackId], or of the whole track when `null`. */
    suspend fun removeTrackClip(trackId: TrackId, notationId: NotationId?)
    suspend fun readTrackClips(musicId: MusicId): List<TrackClip>
}
