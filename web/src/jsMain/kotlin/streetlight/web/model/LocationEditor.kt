package streetlight.web.model

import kampfire.api.toMarkdown
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kampfire.model.toUrl
import koala.dom.MessageStore
import koala.model.dedupNotNull
import kampfire.model.tapOf
import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.UrlParseRequest
import streetlight.model.data.mergeLeft
import streetlight.model.external.Address
import streetlight.web.io.ApiClient

/** Creates or updates a location. */
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

    val editField = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val imageField = editField.mutableTapOf({ it.image }) { copy(image = it) }
    val nameField = editField.mutableTapOf({ it.name ?: "" }) { copy(name = it) }
    val addressField = editField.mutableTapOf({ it.address ?: "" }) { copy(address = it) }
    val descriptionField = editField.mutableTapOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val cityField = editField.mutableTapOf({ it.city ?: "" }) { copy(city = it) }
    val eventsUrlField = editField.mutableTapOf({ it.eventsUrl?.value ?: "" }) { copy(eventsUrl = it.toUrl()) }
    val validityField = editField.tapOf { it.validity }
    val websiteField = editField.mutableTapOf({ it.website?.value ?: "" }) { copy(website = it.toUrl()) }

    val imageEditor = ImageEditor(imageField, api)
    val websiteMessage = MessageStore()
    val messages = MessageStore()

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

    /** Fills the blanks of the edit from the location's website. */
    fun readWebsite() {
        val website = editNow.website?.takeIf { it.isAbsolute } ?: return
        scope.launch {
            websiteMessage.set("Reading the link, this will take a minute.", true)
            val edit = api.location.parseLocation(UrlParseRequest(website)).toDataOr(websiteMessage) { return@launch }
            state.set { copy(edit = edit.mergeLeft(editNow)) }
            websiteMessage.deliver("Does this information look correct?")
        }
    }

    /** Whether the edit is valid, showing the reason when not. */
    fun isEditValid(): Boolean {
        val validMessage = editNow.validity.message
        messages.set(validMessage)
        return validMessage == null
    }

    fun reset() {
        state.set { initialState }
        messages.clear()
    }

    fun submit() {
        scope.launch {
            submitSuspend()
        }
    }

    /** Saves the location, returning it, or `null` when invalid or failed. */
    suspend fun submitSuspend(): Location? {
        if (!isEditValid()) return null
        imageEditor.finalizeImage(messages)
        val edit = editField.now

        messages.deliverSending()
        return when (editNow.locationId) {
            null -> api.location.createLocation(edit).toDataOrNull(messages)
            else -> api.location.updateLocation(edit).toDataOrNull(messages)
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