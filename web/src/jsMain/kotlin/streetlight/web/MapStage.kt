package streetlight.web

import kabinet.model.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import streetlight.model.data.Event

class MapStage {

    protected val _state = MutableStateFlow(MapStageState())

    val stateFlow get() = _state.asStateFlow()
    val stateNow get() = _state.value

    fun setState(setter: (MapStageState) -> MapStageState) {
        _state.value = setter(_state.value)
    }

    fun setLocation(point: GeoPoint) {
        val queriedLocation = if (point.distanceTo(stateNow.queriedLocation) < 1000) stateNow.queriedLocation else point
        setState { it.copy(location = point, queriedLocation = queriedLocation)}
    }
}

data class MapStageState(
    val location: GeoPoint = GeoPoint.Denver,
    val queriedLocation: GeoPoint = GeoPoint.Denver,
    val zoom: Int = 11,
    val events: List<Event> = emptyList(),
    val name: String = "Luke",
    val isWarm: Boolean = true,
)


