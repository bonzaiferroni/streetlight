package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.MusicId
import streetlight.model.music.NotationId
import streetlight.model.music.Study
import streetlight.model.music.TrackId

/** The studies of a [StudioDb]. */
interface StudyDao {
    fun flowStudies(): Flow<List<Study>>

    /** The studies of the tracks of the music [musicId]. */
    fun flowStudies(musicId: MusicId): Flow<List<Study>>

    /** The study of the notation [notationId] of the track [trackId], or of the whole track when `null`. */
    fun flowStudy(trackId: TrackId, notationId: NotationId?): Flow<Study?>

    /** Stores [study] in place of any study of its track or notation. */
    suspend fun upsert(study: Study)

    /** Removes the study of the notation [notationId] of the track [trackId], or of the whole track when `null`. */
    suspend fun remove(trackId: TrackId, notationId: NotationId?)
}
