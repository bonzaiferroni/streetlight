package streetlight.web

import koala.dom.UIMessage
import koala.dom.UIMessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import streetlight.model.Api
import streetlight.model.data.EventType
import streetlight.model.data.LocationId
import streetlight.model.data.NewEvent
import streetlight.model.data.NewLocation

class EventCreator(
    scope: CoroutineScope,
    private val client: ClientContext,
    private val eventMap: EventMap,
): BrowserModel<EventCreatorState>(EventCreatorState(), scope) {

    init {
        viewModelScope.launch {
            eventMap.stateFlow.mapDistinct { it.focus.location?.name }.filterNotNull().collect { locationName ->
                setState { it.copy(locationName = locationName) }
            }
        }
    }

    fun toggle(isCreatingEvent: Boolean? = null) {
        setState { it.copy(isCreatingEvent = isCreatingEvent ?: !stateNow.isCreatingEvent) }
    }

    fun setEventName(name: String) {
        setState { it.copy(title = name) }
    }

    fun setLocationName(name: String) {
        setState { it.copy(locationName = name) }
    }

    fun createEvent() {
        viewModelScope.launch {
            val location = eventMap.stateNow.focus.location
            val locationId = location?.locationId
                ?: client.location.createLocation(NewLocation(
                    name = stateNow.locationName,
                    geoPoint = eventMap.stateNow.center,
                ))

            if (locationId == null) {
                setState { it.copy(message = UIMessage(UIMessageType.Error, "Unable to create location")) }
                return@launch
            }

            val eventType = stateNow.eventType
            if (eventType == null) {
                setState { it.copy(message = UIMessage(UIMessageType.Error, "Event type is required")) }
                return@launch
            }

            val newEvent = NewEvent(
                locationId = locationId,
                title = stateNow.title,
                startsAt = Clock.System.now(),
                eventType = eventType
            )

            val event = client.event.create(newEvent)
            console.log(event)
            setState { it.copy(
                isCreatingEvent = false,
                eventType = null,
                locationName = "",
                title = "",
            )}
        }

    }

    fun queryLocation() {
        viewModelScope.launch {
            val center = eventMap.stateNow.center
            val returned = client.location.readPlaceInfo(center)
            setState { it.copy(locationName = returned.displayName)}
        }
    }
}

data class EventCreatorState(
    val isCreatingEvent: Boolean = false,
    val title: String = "",
    val eventType: EventType? = null,
    val locationName: String = "",
    val message: UIMessage? = null,
) {
    val canCreateEvent get() = locationName.isNotBlank() && title.isNotBlank() && eventType != null
}