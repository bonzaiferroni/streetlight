package streetlight.web.model

import kabinet.utils.replaceAt
import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.tap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import streetlight.model.data.Location
import streetlight.model.data.EventEdit
import kotlinx.datetime.LocalDate
import streetlight.model.data.Event
import streetlight.model.data.ExtraLink
import streetlight.model.data.LocationId
import streetlight.model.data.UrlParseRequest
import streetlight.model.data.mergeRight
import streetlight.web.io.ApiClient

class EventEditor(
    initialEvent: EventEdit?,
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(EventEditorState(
        edit = initialEvent ?: EventEdit(),
        costString = initialEvent?.cost?.toString() ?: "",
    ))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val message = MessageStore()
    val urlMessage = MessageStore()
    val imageEditor = ImageEditor(initialEvent?.image, api)

    val editFlow = state.flow.tap { it.edit }
    val startTimeFlow = editFlow.tap { it.startTime }
    val endTimeFlow = editFlow.tap { it.endTime }
    val dateFlow = editFlow.tap { it.date }
    val startsAtFlow = editFlow.tap { it.startsAt }
    val descriptionFlow = stateFlow.tap { it.edit.description ?: "".toMarkdown() }
    val titleFlow = stateFlow.tap { it.edit.title ?: "" }
    val urlFlow = stateFlow.tap { it.edit.website }
    val isFreeFlow = stateFlow.tap { it.edit.isFree }
    val costFlow = stateFlow.tap { it.costString }
    val validityFlow = stateFlow.tap { it.edit.validity }

    val editNow get() = stateNow.edit

    fun setTitle(value: String) = setEvent { it.copy(title = value)}
    fun setStartTime(value: LocalTime) = setEvent { it.copy(startTime = value) }
    fun setEndTime(value: LocalTime) = setEvent { it.copy(endTime = value) }
    fun setDate(value: LocalDate) = setEvent { it.copy(date = value) }
    fun setDescription(value: Markdown) = setEvent { it.copy(description = value) }
    fun setUrl(value: String) = setEvent { it.copy(website = value) }
    fun setFree(value: Boolean) = setEvent { it.copy(cost = if (value) 0f else null)}
    fun setOriginalSourceLabel(value: String) = state.set { it.copy(originalSourceLabel = value) }
    fun setOriginalSourceUrl(value: String) = state.set { it.copy(originalSourceUrl = value) }
    fun setLocationId(value: LocationId?) = setEvent { it.copy(locationId = value) }

    fun addLink(value: ExtraLink) {
        val linksNow = stateNow.edit.links ?: emptyList()
        setEvent { it.copy(links = linksNow + value) }
    }

    fun setCost(value: String) {
        setEvent { it.copy(cost = value.toFloatOrNull())}
        state.set { it.copy(costString = value)}
    }

    fun removeLink(value: ExtraLink) {
        val linksNow = stateNow.edit.links ?: emptyList()
        setEvent { it.copy(links = (linksNow - value).takeIf { links -> links.isNotEmpty() }) }
    }

    fun editLink(index: Int, value: ExtraLink) {
        val linksNow = stateNow.edit.links ?: error("no links to edit")
        setEvent { it.copy(links = linksNow.replaceAt(index, value)) }
    }

    fun setEdit(value: EventEdit) {
        if (value == stateNow.edit) return
        setEvent { value }
    }

    fun isEditValid(): Boolean {
        val validMessage = editNow.validity.message
        message.receive(validMessage)
        return validMessage == null
    }

    fun submit() {
        scope.launch {
            submitSuspend()
        }
    }

    fun readUrl() {
        val url = state.now.edit.website?.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            urlMessage.set("Reading the link, this will take a minute.", true)
            api.parseSingleEvent(UrlParseRequest(url)).handleResponse(urlMessage) { edit ->
                val event = edit.mergeRight(state.now.edit)
                urlMessage.receive("Does this information look correct?")
                state.set { it.copy(edit = event) }
            }
        }
    }

    suspend fun submitSuspend(): Event? {
        if (!isEditValid()) return null
        val image = imageEditor.finalizeImage(message)
        val edit = editNow.copy(image = image)

        message.set("Sending...", true)
        return when (editNow.eventId) {
            null -> api.createEvent(edit)
            else -> api.updateEvent(edit)
        }.handleResponse(message)
    }

    private fun setEvent(provideEvent: (EventEdit) -> EventEdit) {
        state.set { it.copy(edit = provideEvent(editNow)) }
    }
}

data class EventEditorState(
    val edit: EventEdit,
    val possibleLocations: List<Location> = emptyList(),
    val isVisible: Boolean = false,
    val originalSourceLabel: String = "",
    val originalSourceUrl: String = "",
    val costString: String = "",
)