package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import streetlight.model.data.Location
import streetlight.model.data.EventEdit
import streetlight.model.data.Place
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import streetlight.model.external.Address
import streetlight.model.external.OSMPlace
import streetlight.model.external.toGeoPoint
import streetlight.model.utils.toLocalDateTime
import streetlight.model.utils.tomorrowNoon

class EventEditor(
    initialEvent: EventEdit?,
    private val scope: CoroutineScope,
    private val client: ClientContext,
) {
    private val state = storeOf(EventEditorState(initialEvent ?: EventEdit()))
    private val stateFlow = state.flow
    val editFlow = state.flow.mapDistinct { it.event }
    private val stateNow get() = state.now
//    val message = storeOf(UIMessage())

    val imageUrlFlow = stateFlow.mapDistinct { it.event.imageUrl }
    val datetimeFlow = stateFlow.mapDistinct { it.event.startsAt?.toLocalDateTime() }
    val timeFlow = datetimeFlow.mapDistinct { it?.time ?: tomorrowNoon().toLocalDateTime().time }
    val dateFlow = datetimeFlow.mapDistinct { it?.date ?: tomorrowNoon().toLocalDateTime().date }
    val descriptionFlow = stateFlow.mapDistinct { it.event.description ?: "" }
    val titleFlow = stateFlow.mapDistinct { it.event.title }
    val urlFlow = stateFlow.mapDistinct { it.event.url }

    val eventNow get() = stateNow.event
    val locationNow get() = eventNow.place

    fun setEventTitle(value: String) {
        setEvent { it.copy(title = value)}
    }

    fun setTime(value: LocalTime) {
        setEvent { it.copy(startTime = value) }
    }

    fun setDate(value: LocalDate) {
        setEvent { it.copy(date = value) }
    }

    fun setDescription(value: String) {
        setEvent { it.copy(description = value) }
    }

    fun setUrl(value: String) {
        setEvent { it.copy(url = value) }
    }

    fun setEdit(value: EventEdit) {
        if (value == stateNow.event) return
        setEvent { value }
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