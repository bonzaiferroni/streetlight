package streetlight.web.model

import kampfire.api.toMarkdown
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.dedup
import koala.model.dedupNotNull
import koala.model.fieldOf
import koala.model.mutableFieldOf
import koala.model.reactIn
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.UrlParseRequest
import streetlight.model.data.mergeLeft
import streetlight.model.external.Address
import streetlight.web.io.ApiClient

class LocationEditor(
    initialData: LocationEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val initialState = LocationEditorState(initialData)
    private val state = storeOf(initialState)
    val stateNow get() = state.now
    val stateFlow = state.flow

    val editNow get() = state.now.edit
    val editFlow = state.flow.dedupNotNull { it.edit }

    val editField = state.mutableFieldOf({ it.edit }) { copy(edit = it) }
    val imageField = editField.mutableFieldOf({ it.image }) { copy(image = it) }
    val nameField = editField.mutableFieldOf({ it.name ?: "" }) { copy(name = it) }
    val addressField = editField.mutableFieldOf({ it.address ?: "" }) { copy(address = it) }
    val descriptionField = editField.mutableFieldOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val cityField = editField.mutableFieldOf({ it.city ?: "" }) { copy(city = it) }
    val eventsUrlField = editField.mutableFieldOf({ it.eventsUrl ?: "" }) { copy(eventsUrl = it) }
    val validityField = editField.fieldOf { it.validity }
    val websiteField = editField.mutableFieldOf({ it.website ?: "" }) { copy(website = it) }

    val imageEditor = ImageEditor(imageField, api)
    val websiteMessage = MessageStore()
    val message = MessageStore()

    init {
        websiteField.reactIn(scope) { websiteMessage.clear() }
    }

    // fun setWebsite(value: String?) {
    //     if (value == editNow.website) return
    //     setEdit { it.copy(website = value) }
    //     websiteMessage.clear()
    // }

    // fun setEdit(block: (LocationEdit) -> LocationEdit) {
    //     state.set { copy(edit = block(it.edit)) }
    // }

    fun readWebsite() {
        val website = editNow.website?.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            websiteMessage.set("Reading the link, this will take a minute.", true)
            api.parseLocation(UrlParseRequest(website)).handleResponse(websiteMessage) { edit ->
                state.set { copy(edit = edit.mergeLeft(editNow)) }
                websiteMessage.receive("Does this information look correct?")
            }
        }
    }

    fun isEditValid(): Boolean {
        val validMessage = editNow.validity.message
        message.receive(validMessage)
        return validMessage == null
    }

    fun reset() {
        state.set { initialState }
        message.clear()
    }

    fun submit() {
        scope.launch {
            submitSuspend()
        }
    }

    suspend fun submitSuspend(): Location? {
        if (!isEditValid()) return null
        val image = imageEditor.finalizeImage(message)
        val edit = editNow.copy(image = image)

        message.set("Sending...", true)
        return when (editNow.locationId) {
            null -> api.createLocation(edit).handleResponse(message)
            else -> api.updateLocation(edit).handleResponse(message)
        }
    }
}

data class LocationEditorState(
    val edit: LocationEdit,
    val query: String = "",
    val isWorking: Boolean = false,
)

private fun Address.toBasicString(): String? {
    val road = road ?: return null
    return if (number != null) "$number $road" else road
}