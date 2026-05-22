package streetlight.web.model

import kampfire.model.Labeled
import kampfire.model.handleResponse
import koala.dom.messageStore
import koala.dom.set
import koala.model.GeoMap
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
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
    val mapMessage = messageStore()
    val postMessage = messageStore()

    val queryFlow = stateFlow.mapDistinct { it.query }
    val cityFlow = stateFlow.mapDistinct { it.city }
    val locationsFlow = stateFlow.mapDistinct { it.locations }
    val locationFlow = stateFlow.mapDistinct { it.location }
    val osmLocationsFlow = stateFlow.mapDistinct { it.osmLocations }
    val hasOsmLocations = stateFlow.mapDistinct { it.osmLocations.isNotEmpty() }
    val stageFlow = stateFlow.mapDistinct { it.stage }
    val postFlow = stateFlow.mapDistinct { it.post }
    val modeFlow = stateFlow.mapDistinct { it.mode }
    val mapLocationFlow = stateFlow.mapDistinct { it.mapLocation }

    init {
        scope.launch {
            launch {
                queryFlow.collect { query ->
                    api.searchLocations(query, stateNow.city?.takeIf { it.isNotBlank() })
                        .handleResponse(toaster::toast) { locations ->
                            state.set { it.copy(locations = locations) }
                        }
                }
            }

            launch {
                geo.stateFlow.filter { !it.isMoving && stateNow.mode == LocationScoutMode.Map }
                    .mapDistinct { it.center }.collect { center ->
                        osm.readLocationAt(center).handleResponse(mapMessage::set) { location ->
                            val location = location.toEditOrNull() ?: return@handleResponse
                            mapMessage.set(location.displayTitle)
                            state.set { it.copy(mapLocation = location)}
                        }
                    }
            }
        }
    }

    fun setQuery(value: String) = state.set { it.copy(query = value) }
    fun setCity(value: String) = state.set { it.copy(city = value) }
    fun setLocation(value: Location?) = state.set { it.copy(location = value, stage = LocationScoutStage.Post) }
    fun setMode(value: LocationScoutMode) = state.set { it.copy(mode = value) }
    fun setStage(value: LocationScoutStage) = state.set { it.copy(stage = value) }

    fun stageLocation(value: LocationEdit?) {
        if (value == null) return
        editor.setEdit { value.mergeLeft(it) }
        editor.readWebsite()
        state.set { it.copy(osmLocations = emptyList(), stage = LocationScoutStage.Edit) }
    }

    fun stageLocationFromMap() = stageLocation(stateNow.mapLocation)

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

    fun review() {

        state.set { it.copy(stage = LocationScoutStage.Post) }
    }

    fun postToGalaxy() {
        scope.launch {
            postMessage.set("Posting...")
            val location = when (val location = stateNow.location) {
                null -> editor.submitSuspended()
                else -> location
            } ?: return@launch

            val edit = LocationPostEdit(null, galaxy.galaxyId, location.locationId, null)
            api.postLocation(edit).handleResponse(postMessage::set, "Posted location to ${galaxy.name}.") { post ->
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
    val stage: LocationScoutStage = LocationScoutStage.Search,
    val post: Post? = null,
    val mapLocation: LocationEdit? = null,
    val mode: LocationScoutMode = LocationScoutMode.Map,
)

enum class LocationScoutStage: Labeled {
    Search,
    Edit,
    Post;

    override val label get() = name
}

enum class LocationScoutMode {
    Search,
    Map,
}