package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.Community
import streetlight.model.data.NewArea

class StreetListModel(private val app: AppProvider = RuntimeProvider): StateModel<StreetListState>() {

    private val client = app.repo.area

    override val state = ModelState(StreetListState())

    init {
        refreshItems()
    }

    fun setNewStreetName(name: String) {
        setState { it.copy(newStreetName = name, isValidNewItem = name.isNotBlank()) }
    }

    fun createNewStreet() {
        if (!stateNow.isValidNewItem) return
        viewModelScope.launch {
            client.create(NewArea(
                name = stateNow.newStreetName
            ))
            setNewStreetName("")
            refreshItems()
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            val areas = client.readAll() ?: return@launch
            setState { it.copy(communities = areas) }
        }
    }
}

data class StreetListState(
    val communities: List<Community> = emptyList(),
    val newStreetName: String = "",
    val isValidNewItem: Boolean = false,
)