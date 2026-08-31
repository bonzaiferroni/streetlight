package streetlight.web.model

import kampfire.model.toDataOr
import koala.model.dedup
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.NewSong
import streetlight.model.data.Song
import streetlight.web.io.ApiClient

class MusicianHub(
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private val song = storeOf(NewSong())
    private val view = storeOf(MusicHubState())

    val titleFlow = song.flow.dedup { it.title }
    val artistFlow = song.flow.dedup { it.artist }
    val songsFlow = view.flow.dedup { it.songs }

    init {
        refreshSongs()
    }

    fun refreshSongs() {
        scope.launch {
            val songs = api.readSongs().toDataOr { return@launch }
            view.set { copy(songs = songs) }
        }
    }

    fun setArtist(artist: String) {
        song.set { copy(artist = artist) }
    }

    fun setSongTitle(title: String) {
        song.set { copy(title = title) }
    }

    fun addSong() {
        val songNow = song.now
        if (!songNow.isValid) return
        scope.launch {
            api.createSong(songNow).toDataOr { return@launch }
            refreshSongs()
            song.set { NewSong() }
        }
    }
}

data class MusicHubState(
    val songs: List<Song> = emptyList(),
)