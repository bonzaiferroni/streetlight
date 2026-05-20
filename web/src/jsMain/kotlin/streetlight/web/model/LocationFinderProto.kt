@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.distanceTo
import kampfire.model.handleResponse
import kampfire.model.kilometers
import koala.SvgFile
import koala.dom.UIMessage
import koala.dom.set
import koala.model.PanPoint
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.PlaceProto
import streetlight.model.data.UrlParseRequest
import streetlight.model.data.mergeLeft
import streetlight.model.data.mergeRight
import streetlight.model.data.toEdit
import streetlight.model.external.toPlaceProto
import streetlight.web.ui.ViewModel

class LocationFinderProto(
    private val scope: CoroutineScope,
    override val app: Streetlight,
) : ViewModel {

    private val state = storeOf(LocationFinderProtoState())
    private val msg = storeOf<UIMessage?>(UIMessage(introMsg))
    val messageFlow = msg.flow
    val stateFlow = state.flow
    val stateNow get() = state.now

    init {
        scope.launch {
            stateFlow.mapDistinct { it.query }.debounce(500).collect { query ->
                val locations = if (query.isBlank()) {
                    emptyList()
                } else {
                    api.searchLocations(query).handleResponse(msg::set) ?: emptyList()
                }
                state.set { it.copy(locations = locations) }
            }
        }
    }

    fun setQuery(value: String) = state.set { it.copy(query = value) }

    fun setLocation(value: Location?) = state.set { it.copy(location = value) }

    fun setWebsite(value: String) = state.set { it.copy(website = value) }

    fun setEdit(value: LocationEdit) = state.set { it.copy(edit = value) }

    fun reset() = state.set { LocationFinderProtoState() }

    fun searchQuery() {
        val query = state.now.query.takeIf { it.isNotBlank() } ?: return
        msg.set("Searching OpenStreetMap: ${state.now.query}")
        scope.launch {
//            val places = osm.readLocations(query)?.map { it.toPlaceProto() }
//                ?.filter { p -> p.geoPoint?.let { gp -> gp.distanceTo(geo.stateNow.center) < 100.kilometers } ?: false }
//            val place = places?.firstOrNull()
//            if (place == null) {
//                msg.set("We couldn't find anything.")
//                return@launch
//            }
//            place.geoPoint?.let {
//                geo.panMap(PanPoint(point = it, zoom = 15f))
//            }
//
//            val edit = place.toEdit().mergeRight(state.now.edit)
//            state.set { it.copy(edit = edit) }
//            msg.set("Is this what you are looking for?")
        }
    }

    fun here() {
        msg.set("Looking for information about that place on OpenStreetMap...")
        val point = geo.stateNow.center
        scope.launch {
//            val place = osm.readPlaceAt(point)?.toPlaceProto()?.copy(geoPoint = point)
//            if (place == null) {
//                // td: handle
//                msg.set("Something went wrong")
//                return@launch
//            }
//            choosePlace(place)
        }
    }

    fun choosePlace(place: PlaceProto) {
        val website = place.website
        val point = place.geoPoint ?: error("geoPoint is null")
        // addConstructionMarker(point)
        val edit = place.toEdit().mergeRight(state.now.edit)
        state.set { it.copy(website = website ?: "", edit = edit, geoPoint = point) }
        if (website != null) {
            msg.set("OSM provided a website for the location, we can try to read it.")
        } else {
            msg.set("If there is a website for this location we can try to read it.")
        }
    }

    // td: should be handled by LocationEditor
    fun readLocationWebsite() {
        val website = state.now.website.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            msg.set("Reading the link, this will take a minute.")
            when (val response = api.parseLocation(UrlParseRequest(website))) {
                is Ok -> {
                    val edit = response.data.mergeLeft(state.now.edit)
                    state.set { it.copy(edit = edit) }
                    val message = response.message ?: "Does this information look correct?"
                    msg.set(message)
                }
                is Problem -> {
                    msg.set(response.message)
                }
                null -> {
                    msg.set("Something went wrong.")
                }
            }
        }
    }

    fun createLocation() {
        val edit = state.now.edit ?: error("edit not found")
        val invalidMsg = edit.invalidMessage
        if (invalidMsg != null) {
            msg.set(invalidMsg)
            return
        }

        msg.set("Creating ${edit.name}...")
        scope.launch {
            val location = api.createOrEditLocation(edit)
            if (location == null) {
                msg.set("Something went wrong")
                return@launch
            }
            // state.set { it.copy(location = location) }
            // msg.set("Created ${location.name}. You can now add events to ${location.name}.")
            // geo.tempEntities(null)
        }
    }

    private fun addConstructionMarker(point: GeoPoint) {
        geo.tempEntities(
            listOf(
                IconEntity("here", SvgFile.Guitar, point)
            )
        )
    }
}

data class LocationFinderProtoState(
    val query: String = "",
    val website: String = "",
    val geoPoint: GeoPoint? = null,
    val edit: LocationEdit? = null,
    val locations: List<Location> = emptyList(),
    val location: Location? = null,
) {
    val stage
        get() = if (location != null) LocationFinderStage.Complete
        else if (edit != null) LocationFinderStage.Edit
        else LocationFinderStage.Place
}

private val introMsg = "Search for a location or find one on the map."

enum class LocationFinderStage {
    Place,
    Edit,
    Complete,
}