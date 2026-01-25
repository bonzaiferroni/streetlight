package streetlight.web

import kampfire.model.GeoPoint
import streetlight.model.data.LocationEventsRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Event

class EventMap(
    viewModelScope: CoroutineScope,
    val eventClient: EventBrowserClient
): BrowserModel<EventMapState>(EventMapState(), viewModelScope) {

    fun setName(name: String) {
        setState { it.copy(name = name) }
    }

    fun setZoom(zoom: Float) {
        setState { it.copy(zoom = zoom) }
    }

    fun setLocation(point: GeoPoint) {
        if (point.distanceTo(stateNow.queriedLocation) < 1000) {
            setState { it.copy(location = point)}
            return
        }
        viewModelScope.launch {
            val events = eventClient.readLocationEvents(LocationEventsRequest(point, stateNow.zoom))
            setState { it.copy(location = point, queriedLocation = point, events = events)}
        }
    }
}

data class EventMapState(
    val location: GeoPoint = GeoPoint.Denver,
    val queriedLocation: GeoPoint = GeoPoint.Denver,
    val zoom: Float = 11f,
    val events: List<Event> = emptyList(),
    val name: String = ""
)


