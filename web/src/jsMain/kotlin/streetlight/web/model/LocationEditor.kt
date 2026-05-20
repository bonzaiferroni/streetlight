package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.Url
import kampfire.model.handleResponse
import koala.dom.UIMessageType
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.ResourceType
import streetlight.model.data.mergeLeft
import streetlight.model.data.toEdit
import streetlight.model.external.Address
import streetlight.model.external.OSMLocation
import streetlight.model.external.OSMQuery
import streetlight.web.io.ApiClient
import streetlight.web.io.OSMClient

class LocationEditor(
    initialData: LocationEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(LocationEditorState(initialData))
    val stateNow get() = state.now
    val stateFlow = state.flow

    val editNow get() = state.now.edit
    val editFlow = state.flow.mapNotNull { it.edit }.distinctUntilChanged()
//     override val placeFlow = editFlow.mapDistinct { it.toPlace() }

    val nameFlow = editFlow.mapDistinct { it.name }
    val addressFlow = editFlow.mapDistinct { it.address }
    val cityFlow = editFlow.mapDistinct { it.city }
    val descriptionFlow = editFlow.mapDistinct { it.description }
    val websiteFlow = editFlow.mapDistinct { it.website }
    val linksFlow = editFlow.mapDistinct { it.eventsUrl }
    val imageUrlFlow = stateFlow.mapDistinct { it.imageUrl }

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

    fun setLink(value: String?) {
        setEdit { it.copy(website = value) }
    }

    fun setEventsLink(value: String?) {
        setEdit { it.copy(eventsUrl = value) }
    }

    fun setImageUrl(value: Url?) {
        state.set { it.copy(imageUrl = value) }
    }

    fun setCity(value: String) {
        setEdit { it.copy(city = value) }
    }

    fun setEdit(block: (LocationEdit) -> LocationEdit) {
        state.set { it.copy(edit = block(it.edit)) }
    }

    fun postLocation() {
        val edit = editNow.takeIf { it.isValid } ?: return
        scope.launch {
            api.createOrEditLocation(edit).handleResponse(toaster::toast) { location ->
                state.set { it.copy(location = location) }
            }
        }
    }
}

data class LocationEditorState(
    val edit: LocationEdit,
    val imageUrl: Url? = edit.imageRef,
    val location: Location? = null,
    val query: String = "",
)

private fun Address.toBasicString(): String? {
    val road = road ?: return null
    return if (number != null) "$number $road" else road
}