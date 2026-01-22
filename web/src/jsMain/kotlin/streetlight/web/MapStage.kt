package streetlight.web

import kabinet.model.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import streetlight.model.data.Event

class MapStage(
    private val viewModelScope: CoroutineScope,
    private val app: BrowserProvider
) {
    protected val _state = MutableStateFlow(MapStageState())

    val stateFlow get() = _state.asStateFlow()
    val stateNow get() = _state.value

    fun setState(setter: (MapStageState) -> MapStageState) {
        _state.value = setter(_state.value)
    }

    fun setLocation(point: GeoPoint) {
        if (point.distanceTo(stateNow.queriedLocation) < 1000) {
            setState { it.copy(location = point)}
            return
        }
        viewModelScope.launch {
            val events = app.repo.eventClient.readLocationEvents(point)
            setState { it.copy(location = point, queriedLocation = point, events = events)}
        }
    }
}

data class MapStageState(
    val location: GeoPoint = GeoPoint.Denver,
    val queriedLocation: GeoPoint = GeoPoint.Denver,
    val zoom: Int = 11,
    val events: List<Event> = emptyList(),
    val name: String = ""
)


