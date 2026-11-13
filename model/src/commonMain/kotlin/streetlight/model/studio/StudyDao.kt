package streetlight.model.studio

import kotlinx.coroutines.flow.Flow
import streetlight.model.music.MusicId
import streetlight.model.music.Study
import streetlight.model.music.StudyId

/** The studies of a [StudioDb]. */
interface StudyDao {
    fun flowStudies(): Flow<List<Study>>

    /** The studies of the tracks of the music [musicId]. */
    fun flowStudies(musicId: MusicId): Flow<List<Study>>
    fun flowStudy(studyId: StudyId): Flow<Study?>
    suspend fun saveStudy(study: Study)
    suspend fun removeStudy(studyId: StudyId)
}
