package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.NewSong
import streetlight.model.data.Song

class SongFeedModel(
    private val app: AppProvider = RuntimeProvider
): StateModel<SongFeedState>() {

    private val client = app.client.song

    override val state = ModelState(SongFeedState())

    init {
        refreshSongs()
    }

    fun refreshSongs() {
        viewModelScope.launch {
            val songs = client.readSongs() ?: return@launch
            setState { it.copy(songs = songs) }
        }
    }

    fun createItem() {
        if (!stateNow.isValidNewItem) return
        viewModelScope.launch {
            client.createSong(NewSong(
                name = stateNow.newName,
                artist = stateNow.newArtist.takeIf { it.isNotEmpty() }
            ))
            refreshSongs()
        }
    }

    fun setNewName(value: String) {
        setState { it.copy(newName = value, isValidNewItem = value.isNotBlank())}
    }

    fun setNewArtist(value: String) {
        setState { it.copy(newArtist = value) }
    }
}

data class SongFeedState(
    val songs: List<Song> = emptyList(),
    val newName: String = "",
    val newArtist: String = "",
    val isValidNewItem: Boolean = false,
)