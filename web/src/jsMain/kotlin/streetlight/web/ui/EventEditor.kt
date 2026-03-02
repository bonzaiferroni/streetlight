package streetlight.web.ui

import kampfire.model.GeoPoint
import koala.dom.UIMessage
import koala.dom.UIMessageType
import koala.dom.set
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
import streetlight.model.data.Location
import streetlight.model.data.EventEdit
import streetlight.model.data.Place
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import streetlight.model.data.Event
import streetlight.model.data.toPlace
import streetlight.model.external.Address
import streetlight.model.external.OSMPlace
import streetlight.model.external.OSMQuery
import streetlight.model.external.toGeoPoint
import streetlight.model.utils.toLocalDateTime
import streetlight.model.utils.tomorrowNoon

class EventEditor(
    initialEvent: EventEdit,
    private val scope: CoroutineScope,
    private val client: ClientContext,
): PlaceEditor {
    private val state = storeOf(EventEditorState(initialEvent))
    private val stateFlow = state.flow
    private val eventFlow = state.flow.mapDistinct { it.event }
    private val stateNow get() = state.now
    val message = storeOf(UIMessage())

    override val placeFlow = eventFlow.mapDistinct { it.place }

    val imageUrlFlow = stateFlow.mapDistinct { it.event.imageUrl }
    val datetimeFlow = stateFlow.mapDistinct { it.event.startsAt?.toLocalDateTime() }
    val timeFlow = datetimeFlow.mapDistinct { it?.time ?: tomorrowNoon().toLocalDateTime().time }
    val dateFlow = datetimeFlow.mapDistinct { it?.date ?: tomorrowNoon().toLocalDateTime().date }
    val descriptionFlow = stateFlow.mapDistinct { it.event.description ?: "" }
    val titleFlow = stateFlow.mapDistinct { it.event.title }
    val urlFlow = stateFlow.mapDistinct { it.event.url }

    val eventNow get() = stateNow.event
    val locationNow get() = eventNow.place

    private val api get() = client.api

    init {
        scope.launch {
            launch {
                val locationId = eventNow.locationId
                val placeName = eventNow.place?.name
                val geoPoint = eventNow.place?.geoPoint
                if (locationId != null) {
                    val place = api.readLocation(locationId)?.toPlace() ?: return@launch
                    setPlace(place)
                } else if (placeName != null && geoPoint == null) {
                    queryLocation(false)
                }
            }
        }
    }

    fun setEventTitle(value: String) {
        setEvent { it.copy(title = value)}
    }

    override fun setPlaceName(value: String) {
        setPlace { it.copy(name = value) }
    }

    override fun setAddress(value: String) {
        setPlace { it.copy(address = value) }
    }

    override fun setPoint(value: GeoPoint) {
        setPlace { it.copy(geoPoint = value)}
    }

    fun setEventType(value: EventType) {
        setEvent { it.copy(eventType = value) }
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

    suspend fun saveEvent(): Event? {
        val event = eventNow
        if (!event.isValid) return null

        val savedEvent = api.createOrEditEvent(event)?.payload
        return savedEvent
    }

    override fun lookUp() {
        queryLocation(true)
    }

    fun queryLocation(reverse: Boolean) {
        if (reverse) {
            val center = eventNow.place?.geoPoint ?: return
            scope.launch {
                val place = client.location.readPlace(center)
                if (place == null) {
                    message.set("Unable to read place", UIMessageType.Error)
                    return@launch
                }
                setPlace(place)
            }
        } else {
            val location = eventNow.place?.name ?: return
            scope.launch {
                val query = OSMQuery(amenity = location, state = "CO")
                val place = client.location.readPlace(query)?.firstOrNull() ?: return@launch
                setPlace(place)
            }
        }
    }

    fun setImageUrl(url: String?) {
        state.set { it.copy(event = eventNow.copy(imageUrl = url)) }
    }

    private fun setEvent(provideEvent: (EventEdit) -> EventEdit) {
        state.set { it.copy(event = provideEvent(eventNow)) }
    }

    private fun setPlace(provideLocation: (Place) -> Place) {
        setEvent { it.copy(place = provideLocation(locationNow ?: Place())) }
    }

    private fun setPlace(place: OSMPlace) {
        val point = place.toGeoPoint()
        val address = place.address.toBasicString() ?: ""
        val name = place.name.takeIf { it.isNotBlank() } ?: address
        setPlace(Place(name = name, address = address, geoPoint = point))
    }

    private fun setPlace(place: Place) {
        setPlace { place }
    }
}

data class EventEditorState(
    val event: EventEdit,
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