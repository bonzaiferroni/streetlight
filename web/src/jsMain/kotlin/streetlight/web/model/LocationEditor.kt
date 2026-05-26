package streetlight.web.model

import kampfire.api.Slug
import kampfire.model.GeoPoint
import kampfire.model.Url
import kampfire.model.handleResponse
import koala.dom.clear
import koala.dom.MessageStore
import koala.dom.set
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationId
import streetlight.model.data.ResourceType
import streetlight.model.data.UrlParseRequest
import streetlight.model.data.mergeLeft
import streetlight.model.external.Address
import streetlight.web.io.ApiClient

class LocationEditor(
    initialData: LocationEdit,
    scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val scope = CoroutineScope(
        scope.coroutineContext + SupervisorJob(scope.coroutineContext[Job])
    )

    private val initialState = LocationEditorState(initialData)
    private val state = storeOf(initialState)
    val stateNow get() = state.now
    val stateFlow = state.flow

    val websiteMessage = MessageStore()
    val message = MessageStore()

    val editNow get() = state.now.edit
    val editFlow = state.flow.mapNotNull { it.edit }.distinctUntilChanged()
//     override val placeFlow = editFlow.mapDistinct { it.toPlace() }

    val nameFlow = editFlow.mapDistinct { it.name }
    val addressFlow = editFlow.mapDistinct { it.address }
    val cityFlow = editFlow.mapDistinct { it.city }
    val descriptionFlow = editFlow.mapDistinct { it.description }
    val websiteFlow = editFlow.mapDistinct { it.website }
    val linksFlow = editFlow.mapDistinct { it.eventsUrl }
    val imageUrlFlow = editFlow.mapDistinct { it.imageRef }
    val validityFlow = editFlow.mapDistinct { it.validity }

    fun setName(value: String) {
        setEdit { it.copy(name = value) }
    }

    fun setDescription(value: String?) {
        setEdit { it.copy(description = value) }
    }

    fun setAddress(value: String) {
        setEdit { it.copy(address = value) }
    }

    fun setNotes(value: String?) {
        setEdit { it.copy(notes = value) }
    }

    fun setPoint(value: GeoPoint) {
        setEdit { it.copy(geoPoint = value) }
    }

    fun setResources(value: Set<ResourceType>) {
        setEdit { it.copy(resources = value) }
    }

    fun setWebsite(value: String?) {
        if (value == editNow.website) return
        setEdit { it.copy(website = value) }
        websiteMessage.clear()
    }

    fun setEventsLink(value: String?) {
        setEdit { it.copy(eventsUrl = value) }
    }

    fun setImageUrl(value: Url?) {
        setEdit { it.copy(imageRef = value) }
    }

    fun setCity(value: String) {
        setEdit { it.copy(city = value) }
    }

    fun setEdit(block: (LocationEdit) -> LocationEdit) {
        state.set { it.copy(edit = block(it.edit)) }
    }

    fun readWebsite() {
        val website = editNow.website?.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            websiteMessage.set("Reading the link, this will take a minute.")
            api.parseLocation(UrlParseRequest(website)).handleResponse(websiteMessage::set) { edit ->
                state.set { it.copy(edit = edit.mergeLeft(editNow)) }
                websiteMessage.set("Does this information look correct?")
            }
        }
    }

    fun isEditValid(): Boolean {
        val validMessage = editNow.validity.message
        message.set(validMessage)
        return validMessage == null
    }

    fun reset() {
        state.set { initialState }
        scope.coroutineContext.cancelChildren()
        message.clear()
    }

    fun submit() {
        scope.launch {
            submitSuspend()
        }
    }

    suspend fun submitSuspend(): Location? {
        if (!isEditValid() || !uploadImageIfBlob()) return null
        message.set("Sending...")
        return when (editNow.locationId) {
            null -> api.createLocation(editNow).handleResponse(message::set)
            else -> api.updateLocation(editNow).handleResponse(message::set)
        }
    }

    private suspend fun uploadImageIfBlob(): Boolean {
        val blobUrl = editNow.imageRef?.takeIf { it.isBlob } ?: return true
        message.set("Uploading image...")
        val refUrl = api.uploadImage(blobUrl).handleResponse(message::set)
        if (refUrl == null) {
            message.set("Unable to upload image.")
            return false
        }
        setImageUrl(refUrl)
        return true
    }
}

data class LocationEditorState(
    val edit: LocationEdit,
    val query: String = "",
)

private fun Address.toBasicString(): String? {
    val road = road ?: return null
    return if (number != null) "$number $road" else road
}