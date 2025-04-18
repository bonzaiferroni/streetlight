package streetlight.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import pondui.ui.core.StateModel
import streetlight.app.LocationListRoute
import streetlight.app.io.LocationStore
import streetlight.model.data.Location

class LocationListModel(
    route: LocationListRoute,
    store: LocationStore = LocationStore()
): StateModel<LocationListState>(LocationListState()) {
    init {
        viewModelScope.launch {
            val locations = store.readLocations()
        }
    }
}

data class LocationListState(
    val locations: ImmutableList<Location> = persistentListOf()
)