package streetlight.web

import kampfire.model.GeoPoint
import koala.dom.UIMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import streetlight.model.data.EventType
import streetlight.model.data.FileUse
import streetlight.model.data.Location
import streetlight.model.data.NewEvent
import streetlight.model.data.NewLocation
import streetlight.model.data.UserFileRequest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import streetlight.model.utils.toLocalDateTime

class EventCreator(
    scope: CoroutineScope,
    private val client: ClientContext,
    private val geoMap: GeoMap,
): BrowserModel<EventCreatorState>(EventCreatorState(), scope) {

    val urlFlow = stateFlow.mapDistinct { it.imageUrl }
    val userImagesFlow = stateFlow.mapDistinct { it.userImages }
    val locationFlow = stateFlow.mapDistinct { it.location.name }
    val pointFlow = stateFlow.mapDistinct { it.location.geoPoint }
    val addressFlow = stateFlow.mapDistinct { it.location.address ?: "" }
    val datetimeFlow = stateFlow.mapDistinct { it.event.startsAt.toLocalDateTime(timeZone) }
    val timeFlow = datetimeFlow.mapDistinct { it.time }
    val dateFlow = datetimeFlow.mapDistinct { it.date }

    val eventNow get() = stateNow.event
    val locationNow get() = stateNow.location

    init {
        viewModelScope.launch {
            launch {
                geoMap.centerFlow.collect(::setGeoPoint)
            }
            launch {
                val images = client.event.readUserFiles(UserFileRequest(FileUse.EventImage)) ?: emptyList()
                setState { it.copy(userImages = images) }
            }
        }
    }

    fun setEventTitle(value: String) {
        setEvent { it.copy(title = value)}
    }

    fun setLocationName(name: String) {
        setLocation { it.copy(name = name) }
    }

    fun setEventType(value: EventType) {
        setEvent { it.copy(eventType = value) }
    }

    fun setGeoPoint(value: GeoPoint) {
        setLocation { it.copy(geoPoint = value)}
    }

    fun setAddress(value: String) {
        setLocation { it.copy(address = value) }
    }

    fun setTime(value: LocalTime) {
        setEvent { it.copy(startsAt = eventNow.startsAt.withTime(value)) }
    }

    fun setDate(value: LocalDate) {
        setEvent { it.copy(startsAt = eventNow.startsAt.withDate(value)) }
    }

    fun createEvent() {
        viewModelScope.launch {
//            val location = streetMap.stateNow.focus.location
//            val locationId = location?.locationId
//                ?: client.location.createLocation(NewLocation(
//                    name = stateNow.locationName,
//                    geoPoint = streetMap.stateNow.center,
//                ))
//
//            if (locationId == null) {
//                setState { it.copy(message = UIMessage(UIMessageType.Error, "Unable to create location")) }
//                return@launch
//            }
//
//            val newEvent = NewEvent(
//                locationId = locationId,
//                title = stateNow.title,
//                startsAt = Clock.System.now(),
//                eventType = stateNow.eventType
//            )
//
//            val event = client.event.create(newEvent)
//            console.log(event)
//            setState { it.copy(
//                locationName = "",
//                title = "",
//            )}
        }
    }

    fun queryLocation() {
        val center = stateNow.location.geoPoint
        viewModelScope.launch {
            val returned = client.location.readPlaceInfo(center)
//            console.log(prettyJson(returned))
            // val name = returned.name.takeIf { it.isNotBlank() } ?: fromDisplayName(returned)
            val address = returned.address.toBasicString() ?: ""
            val name = returned.name.takeIf { it.isNotBlank() } ?: address
            setLocation { it.copy(name = name, address = address) }
        }
    }

    fun setImageUrl(url: String?) {
        val images = if (url != null) stateNow.userImages + url else stateNow.userImages
        setState { it.copy(imageUrl = url, userImages = images) }
    }

    private fun setEvent(provideEvent: (NewEvent) -> NewEvent) {
        setState { it.copy(event = provideEvent(eventNow)) }
    }

    private fun setLocation(provideLocation: (NewLocation) -> NewLocation) {
        setState { it.copy(location = provideLocation(locationNow)) }
    }
}

data class EventCreatorState(
    val message: UIMessage? = null,
    val imageUrl: String? = null,
    val userImages: List<String> = emptyList(),
    val event: NewEvent = NewEvent(),
    val location: NewLocation = NewLocation(),
    val locations: List<Location> = emptyList()
)

private fun Address.toBasicString(): String? {
    val road = road ?: return null
    return if (number != null) "$number $road" else road
}

private fun Instant.withTime(
    newTime: LocalTime,
): Instant {
    val ldt = this.toLocalDateTime(timeZone)

    val updated = LocalDateTime(
        date = ldt.date,
        time = newTime
    )

    return updated.toInstant(timeZone)
}

private fun Instant.withDate(
    newDate: LocalDate,
): Instant {
    val ldt = this.toLocalDateTime(timeZone)

    val updated = LocalDateTime(
        date = newDate,
        time = ldt.time
    )

    return updated.toInstant(timeZone)
}

private val timeZone = TimeZone.currentSystemDefault()