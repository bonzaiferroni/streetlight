package streetlight.web.model

import kabinet.utils.replaceAt
import kampfire.api.toMarkdown
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.fieldOf
import koala.model.mutableFieldOf
import koala.model.reactIn
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.EventEdit
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
    val editNow get() = stateNow.edit

    val editField = state.mutableFieldOf({ it.edit }) { copy(edit = it) }
    val imageField = editField.mutableFieldOf({ it.image }) { copy(image = it) }
    val startTime = editField.mutableFieldOf({ it.startTime }) { copy(startTime = it)}
    val endTime = editField.mutableFieldOf({ it.endTime }) { copy(endTime = it) }
    val date = editField.mutableFieldOf({ it.date }) { copy(date = it) }
    val startsAt = editField.fieldOf { it.startsAt }
    val description = editField.mutableFieldOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val title = editField.mutableFieldOf({ it.title ?: "" }) { copy(title = it) }
    val urlField = editField.mutableFieldOf({ it.website ?: "" }) { copy(website = it) }
    val isFree = editField.mutableFieldOf({ it.isFree }) { copy(cost = if (it) 0f else null) }
    val costField = state.mutableFieldOf({ it.costString }) { copy(costString = it) }
    val validityCheckField = editField.fieldOf { it.validity }
    val originalSourceLabelField = state.mutableFieldOf({ it.originalSourceLabel }) { copy(originalSourceLabel = it) }
    val originalSourceUrlField = state.mutableFieldOf({ it.originalSourceUrl }) { copy(originalSourceUrl = it) }

    val imageEditor = ImageEditor(imageField, api)
    val message = MessageStore()
    val urlMessage = MessageStore()

    init {
        costField.reactIn(scope) { costString ->
            editField.update { it.copy(cost = costString.toFloatOrNull()) }
        }
    }

    fun setLocationId(value: LocationId?) = editField.update { it.copy(locationId = value) }

    fun addLink(value: ExtraLink) {
        val linksNow = stateNow.edit.links ?: emptyList()
        setEvent { it.copy(links = linksNow + value) }
    }

    fun removeLink(value: ExtraLink) {
        val linksNow = stateNow.edit.links ?: emptyList()
        setEvent { it.copy(links = (linksNow - value).takeIf { links -> links.isNotEmpty() }) }
    }

    fun editLink(index: Int, value: ExtraLink) {
        val linksNow = stateNow.edit.links ?: error("no links to edit")
        setEvent { it.copy(links = linksNow.replaceAt(index, value)) }
    }

    fun isEditValid(): Boolean {
        val validMessage = editNow.validity.message
        message.set(validMessage)
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
                urlMessage.deliver("Does this information look correct?")
                state.set { copy(edit = event) }
            }
        }
    }

    suspend fun submitSuspend(): Event? {
        if (!isEditValid()) return null
        imageEditor.finalizeImage(message)
        val edit = editField.now

        message.set("Sending...", true)
        return when (editNow.eventId) {
            null -> api.createEvent(edit)
            else -> api.updateEvent(edit)
        }.handleResponse(message)
    }

    private fun setEvent(provideEvent: (EventEdit) -> EventEdit) {
        state.set { copy(edit = provideEvent(editNow)) }
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