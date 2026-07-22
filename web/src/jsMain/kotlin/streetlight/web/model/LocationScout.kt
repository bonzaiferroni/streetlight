package streetlight.web.model

import kampfire.model.Labeled
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.dom.launch
import koala.model.GeoCamera
import koala.model.dedup
import koala.model.fieldOf
import koala.model.mutableFieldOf
import koala.model.reactIn
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.PostEdit
import streetlight.model.data.PostId
import streetlight.model.data.PostType
import streetlight.model.data.mergeLeft
import streetlight.model.data.toEditOrNull
import streetlight.web.io.ApiClient
import streetlight.web.io.OSMClient

class LocationScout(
    val galaxy: Galaxy,
    val editor: LocationEditor,
    private val scope: CoroutineScope,
    private val osm: OSMClient,
    private val geo: GeoCamera,
    private val toaster: Toaster,
    private val api: ApiClient,
) {
    private val initialState = LocationScoutState()
    private val state = storeOf(initialState)
    val stateNow get() = state.now
    val stateFlow = state.flow
    val mapMessage = MessageStore()
    val postMessage = MessageStore()
    val queryMessage = MessageStore()

    val queryField = state.mutableFieldOf({ it.query }) { copy(query = it) }
    val cityField = state.mutableFieldOf({ it.city ?: "" }) { copy(city = it) }
    val queryLocationsField = state.fieldOf { it.queryLocations }
    val osmLocationsField = state.fieldOf { it.osmLocations }
    val hasOsmLocationsField = state.fieldOf { it.osmLocations.isNotEmpty() }
    val postField = state.fieldOf { it.postId }
    val modeField = state.mutableFieldOf({ it.mode.ordinal }) { copy(mode = SearchMode.entries[it] ) }
    val mapLocationField = state.mutableFieldOf({ it.mapLocation }) { copy(mapLocation = it) }
    val locationField = state.mutableFieldOf({ it.location }) { copy(location = it, stage = LocationScoutStage.Post) }
    val stageField = state.mutableFieldOf({ it.stage }) { copy(stage = it) }

    init {
        stageField.reactIn(scope) {
            if (it == LocationScoutStage.Search) {
                editor.reset()
            }
        }

        queryField.reactIn(scope) { query ->
            api.searchLocations(query, stateNow.city?.takeIf { it.isNotBlank() })
                .handleResponse(toaster) { locations ->
                    state.set { copy(queryLocations = locations) }
                }
        }

        scope.launch(LocationScout::class) {
            launch("collect geoState") {
                geo.stateFlow.filter { !it.isMoving && stateNow.mode == SearchMode.Map }
                    .dedup { it.center }.collect { center ->
                        osm.readLocationAt(center).handleResponse(mapMessage) { location ->
                            val location = location.toEditOrNull() ?: return@handleResponse
                            mapMessage.receive(location.label)
                            state.set { copy(mapLocation = location)}
                        }
                    }
            }
        }
    }

    // fun setStage(value: LocationScoutStage) {
    //     if (value == LocationScoutStage.Search) {
    //         editor.reset()
    //     }
    //     state.setValue { it.copy(stage = value) }
    // }

    fun stageLocation(value: LocationEdit?) {
        if (value == null) return
        editor.editField.update { value.mergeLeft(it) }
        editor.readWebsite()
        state.set { copy(stage = LocationScoutStage.Edit) }
    }

    fun stageLocationFromMap() = stageLocation(stateNow.mapLocation)

    fun queryOSM() {
        val query = stateNow.query
        if (query.isBlank()) return
        queryMessage.set("Searching...", true)
        scope.launch {
            val city = stateNow.city?.takeIf { it.isNotBlank() }
            val bounds = galaxy.geoBounds.takeIf { city == null }?.resizeBy(5f)
            osm.readLocations(query, stateNow.city, bounds).handleResponse(queryMessage) { locations ->
                queryMessage.receive("found: ${locations.size}")
                state.set { copy(osmLocations = locations.mapNotNull { loc -> loc.toEditOrNull() }) }
            }
        }
    }

    fun review() {
        if (!editor.isEditValid()) return
        state.set { copy(stage = LocationScoutStage.Post) }
    }

    suspend fun submitLocation() = when (val location = stateNow.location) {
        null -> editor.submitSuspend().also { location ->
            state.set { copy(location = location) }
        }
        else -> location
    }

    fun postToGalaxy() {
        scope.launch {
            val location = submitLocation() ?: return@launch

            val edit = PostEdit(null, galaxy.galaxyId, PostType.Location, location.locationId.value, null)
            api.createPost(edit).handleResponse(postMessage) { post ->
                state.set { copy(postId = post.postId) }
            }
        }
    }
}

data class LocationScoutState(
    val query: String = "",
    val city: String? = null,
    val queryLocations: List<Location> = emptyList(),
    val osmLocations: List<LocationEdit> = emptyList(),
    val location: Location? = null,
    val stage: LocationScoutStage = LocationScoutStage.Search,
    val postId: PostId? = null,
    val mapLocation: LocationEdit? = null,
    val mode: SearchMode = SearchMode.Search,
)

enum class LocationScoutStage: Labeled {
    Search,
    Edit,
    Post;

    override val label get() = name
}

enum class SearchMode {
    Search,
    Map,
}