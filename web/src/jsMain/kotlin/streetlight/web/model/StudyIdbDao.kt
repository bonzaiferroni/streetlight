package streetlight.web.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import streetlight.model.music.MusicId
import streetlight.model.music.NotationId
import streetlight.model.music.Study
import streetlight.model.music.Track
import streetlight.model.music.TrackId
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

    override fun flowStudy(trackId: TrackId, notationId: NotationId?) = studies.flowRow(keyOf(trackId, notationId))
    override suspend fun upsert(study: Study) = studies.save(study)
    override suspend fun remove(trackId: TrackId, notationId: NotationId?) = studies.remove(keyOf(trackId, notationId))
}
