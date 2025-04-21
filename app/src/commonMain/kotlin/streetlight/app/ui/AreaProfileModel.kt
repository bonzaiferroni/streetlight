package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kabinet.model.GeoPoint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import pondui.ui.core.StateModel
import streetlight.app.AreaProfileRoute
import streetlight.app.io.LocationStore
import streetlight.model.data.Location
import streetlight.model.data.NewLocation

class AreaProfileModel(
    private val route: AreaProfileRoute,
    private val store: LocationStore = LocationStore()
) : StateModel<AreaProfileState>(AreaProfileState()) {
    init {
        refreshItems()
    }

    fun setNewName(name: String) {
        setState { it.copy(newName = name) }
    }

    fun setNewLatitude(value: String) {
        setState {
            it.copy(
                newLatitude = value,
                isValidNewItem = value.toDoubleOrNull() != null && stateNow.newLongitude.toDoubleOrNull() != null
            )
        }
    }

    fun setNewLongitude(value: String) {
        setState {
            it.copy(
                newLongitude = value,
                isValidNewItem = value.toDoubleOrNull() != null && stateNow.newLatitude.toDoubleOrNull() != null
            )
        }
    }

    fun createNewItem() {
        if (!stateNow.isValidNewItem) return
        viewModelScope.launch {
            store.createLocation(NewLocation(
                areaId = route.areaId,
                name = stateNow.newName.takeIf { it.isNotBlank() },
                geoPoint = GeoPoint(stateNow.newLongitude.toDouble(), stateNow.newLatitude.toDouble())
            ))
            setState { it.copy(newName = "", newLatitude = "", newLongitude = "") }
            refreshItems()
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            val locations = store.readAreaLocations(route.areaId)
                .toImmutableList()
            setState { it.copy(locations = locations) }
        }
    }
}

data class AreaProfileState(
    val locations: ImmutableList<Location> = persistentListOf(),
    val newName: String = "",
    val newLatitude: String = "",
    val newLongitude: String = "",
    val isValidNewItem: Boolean = false,
)