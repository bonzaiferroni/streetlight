package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AreaListRoute
import streetlight.app.io.AreaStore
import streetlight.model.data.Area
import streetlight.model.data.NewArea

class AreaListModel(
    route: AreaListRoute,
    private val store: AreaStore = AreaStore()
): StateModel<AreaListState>() {

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
            store.createArea(NewArea(
                name = stateNow.newAreaName
            ))
            setNewAreaName("")
            refreshItems()
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            val areas = store.readAll() ?: return@launch
            setState { it.copy(areas = areas) }
        }
    }
}

data class AreaListState(
    val areas: List<Area> = emptyList(),
    val newAreaName: String = "",
    val isValidNewItem: Boolean = false,
)