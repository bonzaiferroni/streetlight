package streetlight.web

import kampfire.model.GeoPoint
import koala.dom.UIMessage
import koala.dom.UIMessageType
import koala.dom.set
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
import streetlight.model.data.Place
import streetlight.model.data.UserFileRequest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import streetlight.model.data.Event
import streetlight.model.data.EventParseItem
import streetlight.model.utils.toLocalDateTime
import streetlight.model.utils.tomorrowNoon

class EventEditor(
    private val scope: CoroutineScope,
    private val client: ClientContext,
    private val geoMap: GeoMap,
) {
    private val state = storeOf(EventEditorState())
    private val stateFlow = state.flow
    private val eventFlow = state.flow.mapDistinct { it.event }
    private val stateNow get() = state.now
    val message = storeOf(UIMessage())

    val imageUrlFlow = stateFlow.mapDistinct { it.event.imageUrl }
    val userImagesFlow = stateFlow.mapDistinct { it.userImages }
    val locationFlow = eventFlow.mapDistinct { it.location?.name }
    val pointFlow = eventFlow.mapDistinct { it.location?.geoPoint }
    val addressFlow = eventFlow.mapDistinct { it.location?.address ?: "" }
    val datetimeFlow = stateFlow.mapDistinct { it.event.startsAt?.toLocalDateTime() }
    val timeFlow = datetimeFlow.mapDistinct { it?.time ?: tomorrowNoon().toLocalDateTime().time }
    val dateFlow = datetimeFlow.mapDistinct { it?.date ?: tomorrowNoon().toLocalDateTime().date }
    val descriptionFlow = stateFlow.mapDistinct { it.event.description ?: "" }
    val titleFlow = stateFlow.mapDistinct { it.event.title }
    val urlFlow = stateFlow.mapDistinct { it.event.url }

    val eventNow get() = stateNow.event
    val locationNow get() = eventNow.location

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

    fun initEvent(event: EventEdit) {
        setEvent { event }
        event.location?.let { location ->
            if (location.geoPoint == null) {
                queryLocation(false)
            }
        }
    }

    fun setEventTitle(value: String) {
        setEvent { it.copy(title = value)}
    }

    fun setLocationName(name: String) {
        setPlace { it.copy(name = name) }
    }

    fun setEventType(value: EventType) {
        setEvent { it.copy(eventType = value) }
    }

    fun setGeoPoint(value: GeoPoint) {
        setPlace { it.copy(geoPoint = value)}
    }

    fun setAddress(value: String) {
        setPlace { it.copy(address = value) }
    }

    fun setTime(value: LocalTime) {
        val startsAt = eventNow.startsAt ?: tomorrowNoon()
        setEvent { it.copy(startsAt = startsAt.withTime(value)) }
    }

    fun setDate(value: LocalDate) {
        val startsAt = eventNow.startsAt ?: tomorrowNoon()
        setEvent { it.copy(startsAt = startsAt.withDate(value)) }
    }

    fun setDescription(value: String) {
        setEvent { it.copy(description = value) }
    }

    fun setUrl(value: String) {
        setEvent { it.copy(url = value) }
    }

    fun setEventParse(value: List<EventParseItem>) {
        val value = value.firstOrNull() ?: return
        setEvent { it.copy(
            title = value.name ?: eventNow.title,
            imageUrl = value.imageUrl ?: eventNow.imageUrl,
            startsAt = value.startsAt ?: eventNow.startsAt,
            description = value.description ?: eventNow.description,
            // location
            // address
        )}

    }

    suspend fun saveEvent(): Event? {
        val event = eventNow
        if (!event.isValid) return null

        val savedEvent = api.createOrEdit(event)
        return savedEvent
    }

    fun queryLocation(reverse: Boolean) {
        if (reverse) {
            val center = eventNow.location?.geoPoint ?: return
            scope.launch {
                val place = client.location.readPlace(center)
                if (place == null) {
                    message.set("Unable to read place", UIMessageType.Error)
                    return@launch
                }
                setPlace(place)
            }
        } else {
            val location = eventNow.location?.name ?: return
            scope.launch {
                val query = OSMQuery(amenity = location, state = "CO")
                val place = client.location.readPlace(query)?.firstOrNull() ?: return@launch
                setPlace(place)
            }
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

    private fun setPlace(provideLocation: (Place) -> Place) {
        setEvent { it.copy(location = provideLocation(locationNow ?: Place())) }
    }

    private fun setPlace(place: OSMPlace) {
        val point = place.toGeoPoint()
        val address = place.address.toBasicString() ?: ""
        val name = place.name.takeIf { it.isNotBlank() } ?: address
        setPlace(Place(name = name, address = address, geoPoint = point))
    }

    private fun setPlace(place: Place) {
        place.geoPoint?.let { point ->
            geoMap.panMap(PanPoint(point = point, zoom = 18f))
        }

        setPlace { place }
    }
}

data class EventEditorState(
    val userImages: List<String> = emptyList(),
    val event: EventEdit = EventEdit(),
    val location: Location? = null,
    val possibleLocations: List<Location> = emptyList(),
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