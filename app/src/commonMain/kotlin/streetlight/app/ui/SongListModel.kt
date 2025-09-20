package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.SongListRoute
import streetlight.app.io.SongStore
import streetlight.model.data.NewSong
import streetlight.model.data.Song

class SongListModel(
    route: SongListRoute,
    private val store: SongStore = SongStore()
): StateModel<SongListState>() {

    override val state = ModelState(SongListState())

    init {
        refreshSongs()
    }

    fun refreshSongs() {
        viewModelScope.launch {
            val songs = store.readSongs() ?: return@launch
            setState { it.copy(songs = songs) }
        }
    }

    fun createItem() {
        if (!stateNow.isValidNewItem) return
        viewModelScope.launch {
            store.createSong(NewSong(
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

data class SongListState(
    val songs: List<Song> = emptyList(),
    val newName: String = "",
    val newArtist: String = "",
    val isValidNewItem: Boolean = false,
)