package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import pondui.ui.core.StateModel
import streetlight.app.AreaListRoute
import streetlight.app.io.AreaStore
import streetlight.model.data.Area
import streetlight.model.data.NewArea

class AreaListModel(
    route: AreaListRoute,
    private val store: AreaStore = AreaStore()
): StateModel<AreaListState>(AreaListState()) {
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
            val areas = store.readAll()
                .toImmutableList()
            setState { it.copy(areas = areas) }
        }
    }
}

data class AreaListState(
    val areas: ImmutableList<Area> = persistentListOf(),
    val newAreaName: String = "",
    val isValidNewItem: Boolean = false,
)