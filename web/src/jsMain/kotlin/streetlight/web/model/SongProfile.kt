package streetlight.web.model

import kampfire.model.toDataOr
import koala.model.dedup
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

    val titleFlow = song.flow.dedup { it?.title ?: "" }
    val artistFlow = song.flow.dedup { it?.artist ?: "" }
    val updatedAtFlow = song.flow.dedup { it?.updatedAt }

    init {
        refreshSong()
    }

    fun refreshSong() {
        scope.launch {
            val value = api.readSong(songId).toDataOr { return@launch }
            song.setValue { value }
        }
    }

    fun setTitle(title: String) {
        song.setValue { it?.copy(title = title) }
    }

    fun setArtist(artist: String) {
        song.setValue { it?.copy(artist = artist) }
    }

    fun updateSong() {
        val song = song.now ?: return
        scope.launch {
            val isSuccess = api.updateSong(song).toDataOr { return@launch }
            if (isSuccess) {
                refreshSong()
            }
        }
    }
}