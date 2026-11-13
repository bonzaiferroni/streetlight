package streetlight.web.model

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import streetlight.model.music.MusicId
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.studio.TrackDao
import streetlight.web.idb.IdbTable

/** The [TrackDao] of a [StudioIdbDb]. */
class TrackIdbDao internal constructor(
    private val tracks: IdbTable<Track>,
) : TrackDao {
    override fun flowTracks(musicId: MusicId) = tracks.rowsFlow
        .map { rows -> rows.filter { it.musicId == musicId } }
        .distinctUntilChanged()

    override fun flowTrack(trackId: TrackId) = tracks.flowRow(trackId.toString())
    override suspend fun saveTrack(track: Track) = tracks.save(track)
    override suspend fun removeTrack(trackId: TrackId) = tracks.remove(trackId.toString())
}
