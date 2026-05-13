package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.Url
import koala.dom.UIMessage
import koala.dom.set
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationId
import streetlight.model.data.ResourceType
import streetlight.model.external.Address
import streetlight.web.io.ApiClient
import streetlight.web.io.handleResponse
import streetlight.web.ui.ViewModel

class LocationEditor(
    initialData: LocationEdit?,
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private val state = storeOf(LocationEditorState(initialData))
    val stateNow get() = state.now
    val stateFlow = state.flow

    val editNow get() = state.now.edit
    val editFlow = state.flow.mapNotNull { it.edit }.distinctUntilChanged()
    val msg = storeOf(initialData?.let { UIMessage(it.invalidMessage ?: "Looks good.") })
//     override val placeFlow = editFlow.mapDistinct { it.toPlace() }

    fun setPlaceName(value: String) {
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

    fun setImageRef(value: Url?) {
        setEdit { it.copy(imageRef = value) }
    }

    fun setCity(value: String) {
        setEdit { it.copy(city = value) }
    }

    fun setEdit(block: (LocationEdit) -> LocationEdit) {
        state.set { it.copy(edit = block(it.edit ?: LocationEdit())) }
    }

    fun postLocation() {
        val edit = editNow?.takeIf { it.isValid } ?: return
        msg.set("Posting ${edit.name}...")
        scope.launch {
            api.createOrEditLocation(edit).handleResponse(msg::set) { location ->
                state.set { it.copy(location = location) }
                msg.set("Posted. You can now add events to ${location.name}.")
            }
        }
    }

//    fun lookUp() {
//        queryLocation(false)
//    }

//    fun queryLocation(reverse: Boolean) {
//        val edit = editNow ?: return
//        if (reverse) {
//            val center = edit.geoPoint ?: return
//            scope.launch {
//                val place = client.location.readPlace(center)
//                if (place == null) {
//                    message.set("Unable to read place", UIMessageType.Error)
//                    return@launch
//                }
//                setPlace(place)
//            }
//        } else {
//            val name = edit.name
//            if (name.isNullOrBlank()) return
//            scope.launch {
//                val query = OSMQuery(amenity = name, state = "CO")
//                val place = client.location.readPlaces(query)?.firstOrNull() ?: return@launch
//                setPlace(place)
//            }
//        }
//    }

//    suspend fun saveLocation(): LocationId? {
//        val edit = editNow ?: return null
//        return api.createOrEditLocation(edit)?.locationId
//    }

//    private fun setPlace(place: OSMPlace) {
//        val point = place.toGeoPoint()
//        val address = place.address.toBasicString() ?: ""
//        val name = place.name.takeIf { it.isNotBlank() }
//        setPlace(Place(name = name, address = address, geoPoint = point))
//    }
//
//    private fun setPlace(place: Place) {
//        setEdit { it.copy(
//            name = place.name ?: it.name,
//            address = place.address ?: it.address,
//            geoPoint = place.geoPoint ?: it.geoPoint)
//        }
//    }
}

data class LocationEditorState(
    val edit: LocationEdit? = null,
    val location: Location? = null,
)

private fun Address.toBasicString(): String? {
    val road = road ?: return null
    return if (number != null) "$number $road" else road
}