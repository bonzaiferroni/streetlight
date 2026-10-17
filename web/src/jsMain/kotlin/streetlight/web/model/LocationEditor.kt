package streetlight.web.model

import streetlight.model.data.ImageTarget
import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.toDataOrNull
import kampfire.model.toUrl
import koala.model.MessageStore
import koala.utility.dedupNotNull
import kampfire.model.tapOf
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
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
    val descriptionField = editField.mutableTapOf({ it.description ?: "".toMarkdown() }) { copy(description = it.takeIf(Markdown::isNotBlank)) }
    val cityField = editField.mutableTapOf({ it.city ?: "" }) { copy(city = it) }
    val eventsUrlField = editField.mutableTapOf({ it.eventsUrl?.value ?: "" }) { copy(eventsUrl = it.toUrl()) }
    val validityField = editField.tapOf { it.validity }
    val isHostField = editField.mutableTapOf({ it.isHost == true }) { copy(isHost = it) }
    val websiteField = editField.mutableTapOf({ it.website?.value ?: "" }) { copy(website = it.toUrl()) }

    val imageEditor = ImageEditor(imageField, ImageTarget.Location, api)
    val messages = MessageStore()

    // fun setEdit(block: (LocationEdit) -> LocationEdit) {
    //     state.set { copy(edit = block(it.edit)) }
    // }

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