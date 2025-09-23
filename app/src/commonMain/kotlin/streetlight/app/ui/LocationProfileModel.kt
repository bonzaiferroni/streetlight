package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.LocationProfileRoute
import streetlight.app.RuntimeProvider
import streetlight.model.data.Location
import streetlight.model.data.toProjectId

class LocationProfileModel(
    route: LocationProfileRoute,
    private val app: AppProvider = RuntimeProvider
): StateModel<LocationProfileState>() {

    private val client = app.client.location

    override val state = ModelState(LocationProfileState())

    init {
        viewModelScope.launch {
            val location = client.readLocation(route.id.toProjectId())
            setState { it.copy(location = location, modLocation = location) }
        }
    }

    fun modifyItem(location: Location) {
        setState { it.copy(modLocation = location) }
    }

    fun updateItem() {
        val location = stateNow.modLocation
        if (!stateNow.isValidUpdate || location == null) return
        viewModelScope.launch {
            val isSuccess = client.updateLocation(location) ?: return@launch
            if (isSuccess) {
                setState { it.copy(location = location, modLocation = location) }
            }
        }
    }
}

data class LocationProfileState(
    val location: Location? = null,
    val modLocation: Location? = null,
) {
    val isValidUpdate get() = location != modLocation
}