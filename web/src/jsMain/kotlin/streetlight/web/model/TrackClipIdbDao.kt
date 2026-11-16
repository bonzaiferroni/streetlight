package streetlight.web.model

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import streetlight.model.music.MusicId
import streetlight.model.music.TrackClip
import streetlight.model.music.NotationId
import streetlight.model.music.TrackId
import streetlight.model.studio.TrackClipDao
import streetlight.web.idb.IdbTable

/** The [TrackClipDao] of a [StudioIdbDb]. */
class TrackClipIdbDao internal constructor(
    private val trackClips: IdbTable<TrackClip>,
) : TrackClipDao {
    override fun flowTrackClips(musicId: MusicId) = trackClips.rowsFlow
        .map { rows -> rows.filter { it.musicId == musicId } }
        .distinctUntilChanged()

    override suspend fun saveTrackClip(trackClip: TrackClip) = trackClips.save(trackClip)

    override suspend fun removeTrackClip(trackId: TrackId, notationId: NotationId?) =
        trackClips.remove(keyOf(trackId, notationId))

    override suspend fun readTrackClips(musicId: MusicId) = trackClips.readRows().filter { it.musicId == musicId }
}
