package streetlight.web.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import streetlight.model.music.MusicId
import streetlight.model.music.Study
import streetlight.model.music.StudyId
import streetlight.model.music.Track
import streetlight.model.studio.StudyDao
import streetlight.web.idb.IdbTable

/** The [StudyDao] of a [StudioIdbDb]. */
class StudyIdbDao internal constructor(
    private val studies: IdbTable<Study>,
    private val tracks: IdbTable<Track>,
) : StudyDao {
    override fun flowStudies() = studies.rowsFlow

    override fun flowStudies(musicId: MusicId) = combine(studies.rowsFlow, tracks.rowsFlow) { studyRows, trackRows ->
        val trackIds = trackRows.filter { it.musicId == musicId }.mapTo(mutableSetOf()) { it.trackId }
        studyRows.filter { it.trackId in trackIds }
    }.distinctUntilChanged()

    override fun flowStudy(studyId: StudyId) = studies.flowRow(studyId.toString())
    override suspend fun saveStudy(study: Study) = studies.save(study)
    override suspend fun removeStudy(studyId: StudyId) = studies.remove(studyId.toString())
}
