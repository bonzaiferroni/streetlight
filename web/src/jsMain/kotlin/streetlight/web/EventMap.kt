package streetlight.web

import kampfire.model.GeoBounds
import streetlight.model.data.MapQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.EventLocation
import streetlight.model.data.Location
import streetlight.model.data.NewEvent
import streetlight.model.data.NewLocation

class EventMap(
    viewModelScope: CoroutineScope,
    val eventClient: EventBrowserClient
): BrowserModel<EventMapState>(EventMapState(), viewModelScope) {

    val placeFlow = stateFlow.mapDistinct { it.place }

    fun setName(name: String) {
        setState { it.copy(name = name) }
    }

    fun createLocation(location: NewLocation) {
        setState { it.copy(place = stateNow.place.copy(location = location.toLocation())) }
    }

    fun createEvent(event: NewEvent) {
        val location = stateNow.place.location ?: return
        val event = event.toEvent()
        val eventLocation = EventLocation.from(event, location)
        setState { it.copy(
            events = stateNow.events + eventLocation,
            place = stateNow.place.copy(event = eventLocation)
        )}
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
    val name: String = "",
    val place: Place = Place()
) {
    val center get() = bounds.center
}

data class Place(
    val location: Location? = null,
    val event: EventLocation? = null
)


