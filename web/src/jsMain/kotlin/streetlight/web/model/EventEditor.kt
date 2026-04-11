package streetlight.web.model

import kabinet.utils.replaceAt
import kampfire.model.Url
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalTime
import streetlight.model.data.Location
import streetlight.model.data.EventEdit
import kotlinx.datetime.LocalDate
import streetlight.model.data.ExtraLink
import streetlight.model.external.Address

class EventEditor(
    initialEvent: EventEdit?,
    private val scope: CoroutineScope,
    private val client: ClientContext,
) {
    private val state = storeOf(EventEditorState(initialEvent ?: EventEdit()))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val editFlow = state.flow.mapDistinct { it.event }
    val imageUrlFlow = editFlow.mapDistinct { it.imageRef }
    val startTimeFlow = editFlow.mapDistinct { it.startTime }
    val endTimeFlow = editFlow.mapDistinct { it.endTime }
    val dateFlow = editFlow.mapDistinct { it.date }
    val startsAtFlow = editFlow.mapDistinct { it.startsAt }
    val descriptionFlow = stateFlow.mapDistinct { it.event.description ?: "" }
    val titleFlow = stateFlow.mapDistinct { it.event.title }
    val urlFlow = stateFlow.mapDistinct { it.event.link }
    val isFreeFlow = stateFlow.mapDistinct { it.event.isFree }
    val costFlow = stateFlow.mapDistinct { it.event.cost?.toString() }

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

    fun setImageRef(url: Url?) {
        state.set { it.copy(event = eventNow.copy(imageRef = url)) }
    }

    fun setCost(value: String) {
        setEvent { it.copy(cost = value.toFloatOrNull())}
    }

    fun setFree(value: Boolean) {
        setEvent { it.copy(cost = if (value) 0f else null)}
    }

    fun addLink(value: ExtraLink) {
        val linksNow = stateNow.event.links ?: emptyList()
        setEvent { it.copy(links = linksNow + value) }
    }

    fun removeLink(value: ExtraLink) {
        val linksNow = stateNow.event.links ?: emptyList()
        setEvent { it.copy(links = (linksNow - value).takeIf { links -> links.isNotEmpty() }) }
    }

    fun editLink(index: Int, value: ExtraLink) {
        val linksNow = stateNow.event.links ?: error("no links to edit")
        setEvent { it.copy(links = linksNow.replaceAt(index, value)) }
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