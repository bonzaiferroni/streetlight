package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
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

    val titleFlow = song.flow.mapDistinct { it.title }
    val artistFlow = song.flow.mapDistinct { it.artist }
    val songsFlow = view.flow.mapDistinct { it.songs }

    init {
        refreshSongs()
    }

    fun refreshSongs() {
        scope.launch {
            val songs = api.readSongs() ?: emptyList()
            view.set { it.copy(songs = songs) }
        }
    }

    fun setArtist(artist: String) {
        song.set { it.copy(artist = artist) }
    }

    fun setSongTitle(title: String) {
        song.set { it.copy(title = title) }
    }

    fun addSong() {
        val songNow = song.now
        if (!songNow.isValid) return
        scope.launch {
            val id = api.createSong(songNow)
            if (id != null) {
                refreshSongs()
                song.set { NewSong() }
            }
        }
    }
}

data class MusicHubState(
    val songs: List<Song> = emptyList(),
)