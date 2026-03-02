package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.web.io.ApiClient

class SongProfile(
    private val songId: SongId,
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val song = storeOf<Song?>(null)

    val titleFlow = song.flow.mapDistinct { it?.title ?: "" }
    val artistFlow = song.flow.mapDistinct { it?.artist ?: "" }
    val updatedAtFlow = song.flow.mapDistinct { it?.updatedAt }

    init {
        refreshSong()
    }

    fun refreshSong() {
        scope.launch {
            val value = api.readSong(songId) ?: return@launch
            song.set { value }
        }
    }

    fun setTitle(title: String) {
        song.set { it?.copy(title = title) }
    }

    fun setArtist(artist: String) {
        song.set { it?.copy(artist = artist) }
    }

    fun updateSong() {
        val song = song.now ?: return
        scope.launch {
            val isSuccess = api.updateSong(song) ?: return@launch
            if (isSuccess) {
                refreshSong()
            }
        }
    }
}