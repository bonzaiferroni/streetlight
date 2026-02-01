package streetlight.web

import kampfire.model.GeoBounds
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.MapQuery
import kotlinx.coroutines.launch
import streetlight.model.data.EventInfo
import streetlight.model.data.Location

class EventMap(
    scope: CoroutineScope,
    private val client: ClientContext,
): BrowserModel<EventMapState>(EventMapState(), scope) {

    val focusFlow = stateFlow.mapDistinct { it.focus }

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
                val events = client.event.queryMap(MapQuery(queriedBounds, stateNow.zoom)) ?: emptyList()
                setState { it.copy(areaEvents = events)}
            }
        }
    }

    fun setLocation(value: Location) {
        setState { it.copy(focus = it.focus.copy(location = value)) }
    }
}

data class EventMapState(
    val bounds: GeoBounds = GeoBounds.Denver,
    val queriedBounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val areaEvents: List<EventInfo> = emptyList(),
    val name: String = "",
    val focus: MapFocus = MapFocus(),
) {
    val center get() = bounds.center
}

data class MapFocus(
    val location: Location? = null,
    val event: EventInfo? = null,
)


