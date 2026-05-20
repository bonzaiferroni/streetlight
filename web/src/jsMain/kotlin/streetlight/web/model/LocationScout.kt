package streetlight.web.model

import kampfire.model.handleResponse
import koala.dom.UIMessageType
import koala.model.GeoMap
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationPostEdit
import streetlight.model.data.Post
import streetlight.model.data.mergeLeft
import streetlight.model.data.toEditOrNull
import streetlight.web.io.ApiClient
import streetlight.web.io.OSMClient

class LocationScout(
    val galaxy: Galaxy,
    private val editor: LocationEditor,
    private val scope: CoroutineScope,
    private val osm: OSMClient,
    private val geo: GeoMap,
    private val toaster: Toaster,
    private val api: ApiClient,
) {
    private val state = storeOf(LocationScoutState(city = galaxy.city))
    val stateNow get() = state.now
    val stateFlow = state.flow

    val queryFlow = stateFlow.mapDistinct { it.query }
    val cityFlow = stateFlow.mapDistinct { it.city }
    val locationsFlow = stateFlow.mapDistinct { it.locations }
    val locationFlow = stateFlow.mapDistinct { it.location }
    val osmLocationsFlow = stateFlow.mapDistinct { it.osmLocations }
    val hasOsmLocations = stateFlow.mapDistinct { it.osmLocations.isNotEmpty() }
    val isEditorStaged = stateFlow.mapDistinct { it.isEditorStaged }
    val postFlow = stateFlow.mapDistinct { it.post }

    init {
        scope.launch {
            queryFlow.collect { query ->
                api.searchLocations(query, stateNow.city?.takeIf { it.isNotBlank() })
                    .handleResponse(toaster::toast) { locations ->
                        state.set { it.copy(locations = locations) }
                    }
            }
        }
    }

    fun setQuery(value: String) = state.set { it.copy(query = value) }
    fun setCity(value: String) = state.set { it.copy(city = value) }
    fun setLocation(value: Location?) = state.set { it.copy(location = value)}
    fun setOSMLocation(value: LocationEdit?) {
        if (value == null) return
        editor.setEdit { value.mergeLeft(it) }
        editor.readWebsite()
        state.set { it.copy(osmLocations = emptyList(), isEditorStaged = true) }
    }

    fun queryOSM() {
        val query = stateNow.query
        if (query.isBlank()) return
        scope.launch {
            osm.readLocations(query, stateNow.city).handleResponse(toaster::toast) { locations ->
                if (locations.isEmpty()) {
                    toaster.toast("Location not found: $query.")
                }
                state.set { it.copy(osmLocations = locations.mapNotNull { loc -> loc.toEditOrNull() }) }
            }
        }
    }

    fun reverseQueryOSM() {
        val center = geo.stateNow.center
        scope.launch {
            val location = osm.readPlaceAt(center)
            if (location == null) {
                toaster.toast("Unable to read place", UIMessageType.Error)
                return@launch
            }
            // setEdit(location)
        }
    }

    fun postToGalaxy() {
        scope.launch {
            val location = when(val location = stateNow.location) {
                null -> editor.submitSuspended()
                else -> location
            } ?: return@launch

            val edit = LocationPostEdit(null, galaxy.galaxyId, location.locationId, null)
            api.postLocation(edit).handleResponse(toaster::toast, "Posted location to ${galaxy.name}.") { post ->
                state.set { it.copy(post = post) }
            }
        }
    }
}

data class LocationScoutState(
    val query: String = "",
    val city: String? = null,
    val locations: List<Location> = emptyList(),
    val osmLocations: List<LocationEdit> = emptyList(),
    val location: Location? = null,
    val isEditorStaged: Boolean = false,
    val post: Post? = null,
)