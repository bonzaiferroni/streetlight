package streetlight.web

import koala.dom.UIMessage
import koala.dom.UIMessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import streetlight.model.data.EventType
import streetlight.model.data.FileUse
import streetlight.model.data.NewEvent
import streetlight.model.data.NewLocation
import streetlight.model.data.UserFileRequest

class EventCreator(
    scope: CoroutineScope,
    private val client: ClientContext,
    private val streetMap: StreetMap,
): BrowserModel<EventCreatorState>(EventCreatorState(), scope) {

    val urlFlow = stateFlow.mapDistinct { it.imageUrl }
    val userImagesFlow = stateFlow.mapDistinct { it.userImages }

    init {
        viewModelScope.launch {
            launch {
                streetMap.stateFlow.mapDistinct { it.focus.location?.name }.filterNotNull().collect { locationName ->
                    setState { it.copy(locationName = locationName) }
                }
            }
            launch {
                val images = client.event.readUserFiles(UserFileRequest(FileUse.EventImage)) ?: emptyList()
                setState { it.copy(userImages = images) }
            }
        }
    }

    fun setEventTitle(name: String) {
        setState { it.copy(title = name) }
    }

    fun setLocationName(name: String) {
        setState { it.copy(locationName = name) }
    }

    fun setEventType(value: EventType) {
        setState { it.copy(eventType = value) }
    }

    fun createEvent() {
        viewModelScope.launch {
            val location = streetMap.stateNow.focus.location
            val locationId = location?.locationId
                ?: client.location.createLocation(NewLocation(
                    name = stateNow.locationName,
                    geoPoint = streetMap.stateNow.center,
                ))

            if (locationId == null) {
                setState { it.copy(message = UIMessage(UIMessageType.Error, "Unable to create location")) }
                return@launch
            }

            val newEvent = NewEvent(
                locationId = locationId,
                title = stateNow.title,
                startsAt = Clock.System.now(),
                eventType = stateNow.eventType
            )

            val event = client.event.create(newEvent)
            console.log(event)
            setState { it.copy(
                locationName = "",
                title = "",
            )}
        }
    }

    fun queryLocation() {
        viewModelScope.launch {
            val center = streetMap.stateNow.center
            val returned = client.location.readPlaceInfo(center)
            setState { it.copy(locationName = returned.displayName)}
        }
    }

    fun setImageUrl(url: String?) {
        val images = if (url != null) stateNow.userImages + url else stateNow.userImages
        setState { it.copy(imageUrl = url, userImages = images) }
    }
}

data class EventCreatorState(
    val title: String = "",
    val eventType: EventType = EventType.Show,
    val locationName: String = "",
    val message: UIMessage? = null,
    val imageUrl: String? = null,
    val userImages: List<String> = emptyList()
) {
    val canCreateEvent get() = locationName.isNotBlank() && title.isNotBlank()
}