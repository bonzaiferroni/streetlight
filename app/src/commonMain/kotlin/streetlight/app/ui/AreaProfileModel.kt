package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kabinet.model.GeoPoint
import kotlinx.coroutines.launch
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.AreaProfileRoute
import streetlight.app.RuntimeProvider
import streetlight.model.data.Location
import streetlight.model.data.NewLocation
import streetlight.model.data.toProjectId

class AreaProfileModel(
    private val route: AreaProfileRoute,
    private val app: AppProvider = RuntimeProvider
) : StateModel<AreaProfileState>() {

    private val client = app.client.area

    override val state = ModelState(AreaProfileState())

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
        val name = stateNow.newName.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            app.client.location.createLocation(NewLocation(
                areaId = route.areaId.toProjectId(),
                name = name,
                geoPoint = GeoPoint(stateNow.newLongitude.toDouble(), stateNow.newLatitude.toDouble())
            ))
            setState { it.copy(newName = "", newLatitude = "", newLongitude = "") }
            refreshItems()
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            val locations = app.client.location.readAreaLocations(route.areaId.toProjectId()) ?: return@launch
            setState { it.copy(locations = locations) }
        }
    }
}

data class AreaProfileState(
    val locations: List<Location> = emptyList(),
    val newName: String = "",
    val newLatitude: String = "",
    val newLongitude: String = "",
    val isValidNewItem: Boolean = false,
)