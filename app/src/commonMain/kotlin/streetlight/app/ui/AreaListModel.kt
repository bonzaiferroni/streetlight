package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.Area
import streetlight.model.data.NewArea

class AreaListModel(private val app: AppProvider = RuntimeProvider): StateModel<AreaListState>() {

    private val client = app.client.area

    override val state = ModelState(AreaListState())

    init {
        refreshItems()
    }

    fun setNewAreaName(name: String) {
        setState { it.copy(newAreaName = name, isValidNewItem = name.isNotBlank()) }
    }

    fun createNewArea() {
        if (!stateNow.isValidNewItem) return
        viewModelScope.launch {
            client.createArea(NewArea(
                name = stateNow.newAreaName
            ))
            setNewAreaName("")
            refreshItems()
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            val areas = client.readAll() ?: return@launch
            setState { it.copy(areas = areas) }
        }
    }
}

data class AreaListState(
    val areas: List<Area> = emptyList(),
    val newAreaName: String = "",
    val isValidNewItem: Boolean = false,
)