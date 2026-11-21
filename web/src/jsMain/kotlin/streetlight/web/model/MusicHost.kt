package streetlight.web.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.music.Music
import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.emptyTrackOf
import streetlight.model.music.notationOrNullOf
import streetlight.model.music.orderTracks
import streetlight.model.music.trackOrNullOf
import streetlight.model.studio.StudioDb

class MusicHost(
    private val scope: CoroutineScope,
    initialSheet: MusicSheet,
    private val db: StudioDb,
) {
    val sheetState = storeOf(initialSheet)
    val musicState = sheetState.tapOf { it.music }

    init {
        scope.launch {
            sheetState.saveChanges { sheet -> db.music.saveMusicSheet(sheet) }
        }
    }

    fun update(music: Music) {
        val musicNow = sheetState.now.music
        val tracksNow = sheetState.now.tracks
        val tracks = if (music.trackOrder != musicNow.trackOrder) tracksNow.orderTracks(music.trackOrder) else tracksNow
        sheetState.set { copy(music = music, tracks = tracks) }
    }

    fun update(track: Track) {
        sheetState.set { copy(tracks = tracks.map { if (it.trackId == track.trackId) track else it }) }
    }

    fun add(track: Track) {
        val musicNow = sheetState.now.music
        val music = musicNow.copy(trackOrder = musicNow.trackOrder + track.trackId)
        sheetState.set { copy(tracks = tracks + track, music = music) }
    }

    fun remove(trackId: TrackId) {
        val musicNow = sheetState.now.music
        val music = musicNow.copy(trackOrder = musicNow.trackOrder - trackId)
        sheetState.set { copy(tracks = tracks.filter { it.trackId != trackId }, music = music) }
    }

    fun readNotation(notationId: NotationId) = sheetState.now.notationOrNullOf(notationId) ?: error("notation not found")
    fun readTrack(notationId: NotationId) = sheetState.now.trackOrNullOf(notationId) ?: error("track not found")
    fun readTrack(trackId: TrackId) = sheetState.now.tracks.first { it.trackId == trackId }

    fun trackStateOf(trackId: TrackId) = sheetState.tapOf { sheet ->
        sheet.tracks.firstOrNull { it.trackId == trackId} ?: error("track not found")
    }
}