package streetlight.web.model

import kampfire.api.toMarkdown
import kampfire.model.GeoPoint
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.dedup
import koala.model.dedupNotNull
import koala.model.mutableFieldOf
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.ResourceType
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

    val websiteMessage = MessageStore()
    val message = MessageStore()
    val imageEditor = ImageEditor(initialData.image, api)

    val editNow get() = state.now.edit
    val editFlow = state.flow.dedupNotNull { it.edit }
//     override val placeFlow = editFlow.mapDistinct { it.toPlace() }

    val editField = state.mutableFieldOf({ it.edit }) { copy(edit = it) }

    val nameField = editField.mutableFieldOf({ it.name ?: "" }) { copy(name = it) }
    val addressField = editField.mutableFieldOf({ it.address ?: "" }) { copy(address = it) }
    val descriptionField = editField.mutableFieldOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val cityField = editField.mutableFieldOf({ it.city ?: "" }) { copy(city = it) }
    val eventsUrlField = editField.mutableFieldOf({ it.eventsUrl ?: "" }) { copy(eventsUrl = it) }
    val validityFlow = editFlow.dedup { it.validity }

    val websiteField = editField.mutableFieldOf({ it.website ?: "" }) { value ->
        websiteMessage.clear()
        copy(website = value)
    }

    // fun setName(value: String) = setEdit { it.copy(name = value) }
    // fun setDescription(value: Markdown?) = setEdit { it.copy(description = value) }
    // fun setAddress(value: String) = setEdit { it.copy(address = value) }
    fun setNotes(value: String?) = setEdit { it.copy(notes = value) }
    fun setPoint(value: GeoPoint) = setEdit { it.copy(geoPoint = value) }
    fun setResources(value: Set<ResourceType>) = setEdit { it.copy(resources = value) }
    fun setEventsLink(value: String?) = setEdit { it.copy(eventsUrl = value) }
    // fun setCity(value: String) = setEdit { it.copy(city = value) }

    // fun setWebsite(value: String?) {
    //     if (value == editNow.website) return
    //     setEdit { it.copy(website = value) }
    //     websiteMessage.clear()
    // }

    fun setEdit(block: (LocationEdit) -> LocationEdit) {
        state.setValue { it.copy(edit = block(it.edit)) }
    }

    fun readWebsite() {
        val website = editNow.website?.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            websiteMessage.set("Reading the link, this will take a minute.", true)
            api.parseLocation(UrlParseRequest(website)).handleResponse(websiteMessage) { edit ->
                state.setValue { it.copy(edit = edit.mergeLeft(editNow)) }
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
        state.setValue { initialState }
        scope.coroutineContext.cancelChildren()
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