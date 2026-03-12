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

class EventEditor(
    initialEvent: EventEdit?,
    private val scope: CoroutineScope,
    private val client: ClientContext,
) {
    private val state = storeOf(EventEditorState(initialEvent ?: EventEdit()))
    private val stateFlow = state.flow
    private val stateNow get() = state.now

    val editFlow = state.flow.mapDistinct { it.event }
    val imageUrlFlow = editFlow.mapDistinct { it.imageUrl }
    val startTimeFlow = editFlow.mapDistinct { it.startTime }
    val endTimeFlow = editFlow.mapDistinct { it.endTime }
    val dateFlow = editFlow.mapDistinct { it.date }
    val descriptionFlow = stateFlow.mapDistinct { it.event.description ?: "" }
    val titleFlow = stateFlow.mapDistinct { it.event.title }
    val urlFlow = stateFlow.mapDistinct { it.event.link }

    val eventNow get() = stateNow.event

    fun setEventTitle(value: String) {
        setEvent { it.copy(title = value)}
    }

    fun setStartTime(value: LocalTime) {
        setEvent { it.copy(startTime = value) }
    }

    fun setEndTime(value: LocalTime) {
        setEvent { it.copy(endTime = value) }
    }

    fun setDate(value: LocalDate) {
        setEvent { it.copy(date = value) }
    }

    fun setDescription(value: String) {
        setEvent { it.copy(description = value) }
    }

    fun setUrl(value: String) {
        setEvent { it.copy(link = value) }
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