package streetlight.web.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.MusicSheet
import streetlight.model.music.MusicStudy
import streetlight.model.music.NotationId
import streetlight.model.music.Study
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.orderTracks
import streetlight.model.studio.StudyDao
import streetlight.web.idb.IdbTable

/** The [StudyDao] of a [StudioIdbDb]. */
class StudyIdbDao internal constructor(
    private val studies: IdbTable<Study>,
    private val musics: IdbTable<Music>,
    private val tracks: IdbTable<Track>,
) : StudyDao {
    override fun flowStudies() = studies.rowsFlow

    override fun flowMusicStudies() =
        combine(studies.rowsFlow, musics.rowsFlow, tracks.rowsFlow) { studyRows, musicRows, trackRows ->
            val musicsById = musicRows.associateBy { it.musicId }
            val tracksByMusicId = trackRows.groupBy { it.musicId }
            val tracksById = trackRows.associateBy { it.trackId }
            val sheetsByMusicId = mutableMapOf<MusicId, MusicSheet>()
            studyRows.filter { it.isActive }.mapNotNull { study ->
                val track = tracksById[study.trackId] ?: return@mapNotNull null
                val notationId = study.notationId
                if (notationId != null && track.notations.none { it.notationId == notationId }
                    && track.arrangements.none { it.notationId == notationId }) return@mapNotNull null
                val musicId = track.musicId
                val music = musicsById[musicId] ?: return@mapNotNull null
                val sheet = sheetsByMusicId.getOrPut(musicId) {
                    MusicSheet(music, tracksByMusicId[musicId].orEmpty().orderTracks(music.trackOrder))
                }
                MusicStudy(study, sheet)
            }
        }.distinctUntilChanged()

    override fun flowStudies(musicId: MusicId) = combine(studies.rowsFlow, tracks.rowsFlow) { studyRows, trackRows ->
        val trackIds = trackRows.filter { it.musicId == musicId }.mapTo(mutableSetOf()) { it.trackId }
        studyRows.filter { it.trackId in trackIds }
    }.distinctUntilChanged()

    override fun flowStudy(trackId: TrackId, notationId: NotationId?) = studies.flowRow(keyOf(trackId, notationId))
    override suspend fun upsert(study: Study) = studies.save(study)
    override suspend fun remove(trackId: TrackId, notationId: NotationId?) = studies.remove(keyOf(trackId, notationId))
}
