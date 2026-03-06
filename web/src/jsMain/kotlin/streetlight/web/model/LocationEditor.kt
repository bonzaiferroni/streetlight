package streetlight.web.model

import kampfire.model.GeoPoint
import koala.dom.UIMessage
import koala.dom.UIMessageType
import koala.dom.set
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationId
import streetlight.model.data.ParseRequest
import streetlight.model.data.Place
import streetlight.model.data.ResourceType
import streetlight.model.data.toPlace
import streetlight.model.external.Address
import streetlight.model.external.OSMPlace
import streetlight.model.external.OSMQuery
import streetlight.model.external.toGeoPoint
import streetlight.web.ui.PlaceEditor

class LocationEditor(
    initialData: LocationEdit,
    private val scope: CoroutineScope,
    private val client: ClientContext,
): PlaceEditor {
    private val edit = storeOf(initialData)
    private val editNow get() = edit.now
    val editFlow = edit.flow
    val message = storeOf(UIMessage())
    override val placeFlow = editFlow.mapDistinct { it.toPlace() }

    private val api get() = client.api

    override fun setPlaceName(value: String) {
        edit.set { it.copy(name = value) }
    }

    fun setDescription(value: String?) {
        edit.set { it.copy(description = value) }
    }

    override fun setAddress(value: String) {
        edit.set { it.copy(address = value) }
    }

    fun setNotes(value: String?) {
        edit.set { it.copy(notes = value) }
    }

    override fun setPoint(value: GeoPoint) {
        edit.set { it.copy(geoPoint = value) }
    }

    fun setResources(value: Set<ResourceType>) {
        edit.set { it.copy(resources = value) }
    }

    fun setLink(value: String?) {
        edit.set { it.copy(website = value) }
    }

    fun setEventsLink(value: String?) {
        edit.set { it.copy(eventsLink = value) }
    }

    fun setImageUrl(value: String?) {
        edit.set { it.copy(imageUrl = value) }
    }

    override fun lookUp() {
        queryLocation(false)
    }

    fun queryLocation(reverse: Boolean) {
        if (reverse) {
            val center = editNow.geoPoint ?: return
            scope.launch {
                val place = client.location.readPlace(center)
                if (place == null) {
                    message.set("Unable to read place", UIMessageType.Error)
                    return@launch
                }
                setPlace(place)
            }
        } else {
            val name = editNow.name
            if (name.isNullOrBlank()) return
            scope.launch {
                val query = OSMQuery(amenity = name, state = "CO")
                val place = client.location.readPlaces(query)?.firstOrNull() ?: return@launch
                setPlace(place)
            }
        }
    }

    fun parseLocation() {
        val link = edit.now.website ?: return
        scope.launch {
            val edit = api.parseLocation(ParseRequest(link))
            this@LocationEditor.edit.set { edit ?: editNow }
        }
    }

    suspend fun saveLocation(): LocationId? {
        return api.createOrEditLocation(editNow)?.locationId
    }

    private fun setPlace(place: OSMPlace) {
        val point = place.toGeoPoint()
        val address = place.address.toBasicString() ?: ""
        val name = place.name.takeIf { it.isNotBlank() }
        setPlace(Place(name = name, address = address, geoPoint = point))
    }

    private fun setPlace(place: Place) {
        edit.set { it.copy(
            name = place.name ?: it.name,
            address = place.address ?: it.address,
            geoPoint = place.geoPoint ?: it.geoPoint)
        }
    }
}

private fun Address.toBasicString(): String? {
    val road = road ?: return null
    return if (number != null) "$number $road" else road
}