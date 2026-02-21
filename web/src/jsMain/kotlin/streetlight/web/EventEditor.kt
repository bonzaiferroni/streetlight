package streetlight.web

import kampfire.model.GeoPoint
import koala.dom.UIMessage
import koala.dom.UIMessageType
import koala.model.GeoMap
import koala.model.PanPoint
import koala.model.mapDistinct
import koala.model.storeOf
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
import streetlight.model.data.EventEdit
import streetlight.model.data.NewLocation
import streetlight.model.data.UserFileRequest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import streetlight.model.data.EventId
import streetlight.model.data.toUpdate

class EventEditor(
    private val scope: CoroutineScope,
    private val client: ClientContext,
    private val geoMap: GeoMap,
) {
    private val state = storeOf(EventEditorState())
    private val stateFlow = state.flow
    private val stateNow get() = state.now

    val imageUrlFlow = stateFlow.mapDistinct { it.event.imageUrl }
    val userImagesFlow = stateFlow.mapDistinct { it.userImages }
    val locationFlow = stateFlow.mapDistinct { it.location.name }
    val pointFlow = stateFlow.mapDistinct { it.location.geoPoint }
    val addressFlow = stateFlow.mapDistinct { it.location.address ?: "" }
    val datetimeFlow = stateFlow.mapDistinct { it.event.startsAt.toLocalDateTime(timeZone) }
    val timeFlow = datetimeFlow.mapDistinct { it.time }
    val dateFlow = datetimeFlow.mapDistinct { it.date }
    val descriptionFlow = stateFlow.mapDistinct { it.event.description ?: "" }
    val titleFlow = stateFlow.mapDistinct { it.event.title }
    val messageFlow = stateFlow.mapDistinct { it.message }
    val urlFlow = stateFlow.mapDistinct { it.event.url }

    val eventNow get() = stateNow.event
    val locationNow get() = stateNow.location

    private val api get() = client.api

    init {
        scope.launch {
            launch {
                geoMap.centerFlow.collect(::setGeoPoint)
            }
            launch {
                val images = client.api.readUserFiles(UserFileRequest(FileUse.EventImage)) ?: emptyList()
                state.set { it.copy(userImages = images) }
            }
        }
    }

    fun initEvent(eventId: EventId) {
        scope.launch {
            val event = api.readEvent(eventId) ?: return@launch
            state.set {
                it.copy(
                    event = event.toUpdate(),
                    eventId = eventId,
                )
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

    fun setDescription(value: String) {
        setEvent { it.copy(description = value) }
    }

    fun setUrl(value: String) {
        setEvent { it.copy(url = value) }
    }

    suspend fun saveEvent(): EventId? {
        val location = stateNow.location
        if (!location.isValid) return null
        
        val locationId = stateNow.event.locationId ?: run {
            console.log("creating location")
            api.createLocation(location)
        }
        
        if (locationId == null) {
            state.set { it.copy(message = UIMessage(UIMessageType.Error, "Unable to create/resolve location")) }
            return null
        }

        val eventUpdate = eventNow.copy(locationId = locationId)
        val event = api.create(eventUpdate)
        
        console.log(prettyPrint(event))
        return event?.eventId
    }

    fun queryLocation() {
        val center = stateNow.location.geoPoint
        scope.launch {
            val returned = client.location.readPlaceInfo(center)
//            console.log(prettyJson(returned))
            // val name = returned.name.takeIf { it.isNotBlank() } ?: fromDisplayName(returned)
            geoMap.panMap(PanPoint(point = returned.toGeoPoint(), zoom = 18f))
            val address = returned.address.toBasicString() ?: ""
            val name = returned.name.takeIf { it.isNotBlank() } ?: address
            setLocation { it.copy(name = name, address = address) }
        }
    }

    fun setImageUrl(url: String?) {
        val images = if (url != null) stateNow.userImages + url else stateNow.userImages
        state.set { it.copy(event = eventNow.copy(imageUrl = url), userImages = images) }
    }

    fun setVisibility(value: Boolean) {
        if (value) {
            geoMap.setEntityVisibility { it is EventEntity }
        } else {
            geoMap.setEntityVisibility { true }
        }
        state.set { it.copy(isVisible = value) }
    }

    private fun setEvent(provideEvent: (EventEdit) -> EventEdit) {
        state.set { it.copy(event = provideEvent(eventNow)) }
    }

    private fun setLocation(provideLocation: (NewLocation) -> NewLocation) {
        state.set { it.copy(location = provideLocation(locationNow)) }
    }
}

data class EventEditorState(
    val message: UIMessage? = null,
    val userImages: List<String> = emptyList(),
    val event: EventEdit = EventEdit(),
    val eventId: EventId? = null,
    val location: NewLocation = NewLocation(),
    val locations: List<Location> = emptyList(),
    val isVisible: Boolean = false,
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