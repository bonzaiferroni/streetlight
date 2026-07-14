package streetlight.web.model

import kampfire.model.getDataOrNull
import koala.model.tap
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

    val titleFlow = song.flow.tap { it?.title ?: "" }
    val artistFlow = song.flow.tap { it?.artist ?: "" }
    val updatedAtFlow = song.flow.tap { it?.updatedAt }

    init {
        refreshSong()
    }

    fun refreshSong() {
        scope.launch {
            val value = api.readSong(songId).getDataOrNull() ?: return@launch
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
            val isSuccess = api.updateSong(song).getDataOrNull() ?: return@launch
            if (isSuccess) {
                refreshSong()
            }
        }
    }
}