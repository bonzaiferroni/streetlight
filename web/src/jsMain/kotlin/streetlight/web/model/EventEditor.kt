package streetlight.web.model

import kampfire.api.toMarkdown
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kampfire.model.toUrl
import koala.dom.MessageStore
import koala.model.tapOf
import koala.model.mutableTapOf
import koala.model.reactIn
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.EventEdit
import streetlight.model.data.Event
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

    val editState = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val imageState = editState.mutableTapOf({ it.image }) { copy(image = it) }
    val startTimeState = editState.mutableTapOf({ it.startTime }) { copy(startTime = it)}
    val endTimeState = editState.mutableTapOf({ it.endTime }) { copy(endTime = it) }
    val dateState = editState.mutableTapOf({ it.date }) { copy(date = it) }
    val startsAtState = editState.tapOf { it.startsAt }
    val descriptionState = editState.mutableTapOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val titleState = editState.mutableTapOf({ it.title ?: "" }) { copy(title = it) }
    val urlState = editState.mutableTapOf({ it.website?.value ?: "" }) { copy(website = it.toUrl()) }
    val isFreeState = editState.mutableTapOf({ it.isFree }) { copy(cost = if (it) 0f else null) }
    val costState = state.mutableTapOf({ it.costString }) { copy(costString = it) }
    val validityState = editState.tapOf { it.validity }
    val originalSourceLabelState = state.mutableTapOf({ it.originalSourceLabel }) { copy(originalSourceLabel = it) }
    val originalSourceUrlState = state.mutableTapOf({ it.originalSourceUrl }) { copy(originalSourceUrl = it) }
    val linksState = editState.mutableTapOf({ it.links ?: emptyList() }) { copy(links = it.takeIf { it.isNotEmpty() })}

    val imageEditor = ImageEditor(imageState, api)
    val message = MessageStore()
    val parseMessage = MessageStore()

    init {
        costState.reactIn(scope) { costString ->
            editState.update { it.copy(cost = costString.toFloatOrNull()) }
        }
    }

    fun setLocationId(value: LocationId?) = editState.update { it.copy(locationId = value) }

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

    fun parseFromUrl() {
        val url = state.now.edit.website?.takeIf { it.isAbsolute } ?: return
        scope.launch {
            parseMessage.set("Reading the link, this will take a minute.", true)
            val edit = api.parseSingleEvent(UrlParseRequest(url)).toDataOr(parseMessage) { return@launch }
            val event = edit.mergeRight(state.now.edit)
            parseMessage.deliver("Does this information look correct?")
            state.set { copy(edit = event) }
        }
    }

    suspend fun submitSuspend(): Event? {
        if (!isEditValid()) return null
        imageEditor.finalizeImage(message)
        val edit = editState.now

        message.set("Sending...", true)
        return when (editNow.eventId) {
            null -> api.createEvent(edit)
            else -> api.updateEvent(edit)
        }.toDataOrNull(message)
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