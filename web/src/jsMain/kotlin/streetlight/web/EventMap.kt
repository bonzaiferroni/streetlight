package streetlight.web

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import streetlight.model.data.MapQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Event
import streetlight.model.data.EventLocation

class EventMap(
    viewModelScope: CoroutineScope,
    val eventClient: EventBrowserClient
): BrowserModel<EventMapState>(EventMapState(), viewModelScope) {

    fun setName(name: String) {
        setState { it.copy(name = name) }
    }

    fun setBounds(bounds: GeoBounds, zoom: Float) {
        if (stateNow.queriedBounds.contains(bounds)) {
            setState { it.copy(bounds = bounds, zoom = zoom)}
        } else {
            val queriedBounds = bounds.expandBy(1.5f)
            setState { it.copy(bounds = bounds, zoom = zoom, queriedBounds = queriedBounds )}
            viewModelScope.launch {
                val events = eventClient.queryMap(MapQuery(queriedBounds, stateNow.zoom))
                setState { it.copy(events = events)}
            }
        }
    }
}

data class EventMapState(
    val bounds: GeoBounds = GeoBounds.Denver,
    val queriedBounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val events: List<EventLocation> = emptyList(),
    val name: String = ""
) {
    val center get() = bounds.center
}


