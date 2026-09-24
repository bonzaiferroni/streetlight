package streetlight.web.model

import kampfire.model.Labeled
import kampfire.model.toDataOr
import koala.dom.MessageStore
import koala.utils.launch
import kampfire.model.tapOf
import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationEntity
import streetlight.model.data.PostEdit
import streetlight.model.data.PostId
import streetlight.model.data.PostType
import streetlight.model.data.mergeLeft
import streetlight.model.data.toEditOrNull
import streetlight.web.io.ApiClient
import streetlight.web.io.OSMClient

/**
 * Finds or creates a location, and can post it to [galaxy].
 *
 * A location is found in Streetlight as the query is typed, or in OpenStreetMap on request. The stage follows
 * from the state: nothing chosen is a search, a new location is an edit, a chosen or reviewed one is a review.
 */
class LocationScout(
    val galaxy: Galaxy?,
    val editor: LocationEditor,
    private val scope: CoroutineScope,
    private val osm: OSMClient,
    private val map: MarkerMap,
    private val toaster: Toaster,
    private val api: ApiClient,
    siteConfig: SiteConfig,
) {
    private val initialState = LocationScoutState()
    private val state = storeOf(initialState)
    val stateNow get() = state.now
    val stateFlow = state.flow
    val mapMessage = MessageStore()
    val postMessage = MessageStore()
    val queryMessage = MessageStore()
    private var osmJob: Job? = null
    val postModeState = siteConfig.locationPostModeState

    val queryField = state.mutableTapOf({ it.query }) { copy(query = it) }
    val cityField = state.mutableTapOf({ it.city ?: "" }) { copy(city = it) }
    val locationsState = state.tapOf { it.locations }
    val postField = state.tapOf { it.postId }
    val locationState = state.mutableTapOf({ it.location }) { copy(location = it) }
    val selectionState = state.mutableTapOf<LocationScoutState, LocationEntity?>({ it.location ?: it.edit }) {
        when (it) {
            is Location -> copy(location = it)
            is LocationEdit -> copy(edit = it)
            null -> copy(location = null, edit = null)
        }
    }

    val stageField = state.mutableTapOf({
        if (it.location == null && it.edit == null) LocationScoutStage.Search
        else if (it.location != null || it.isReviewing) LocationScoutStage.Review
        else LocationScoutStage.Edit
    }) {
        when (it) {
            LocationScoutStage.Search -> copy(location = null, edit = null)
            LocationScoutStage.Edit -> {
                if (edit != null) copy(isReviewing = false)
                else this
            }
            LocationScoutStage.Review -> {
                if (edit == null && location == null) this
                else copy(isReviewing = true)
            }
        }
    }

    init {
        stageField.reactIn(scope) {
            if (it == LocationScoutStage.Search) {
                editor.reset()
            }
        }

        state.tapOf { it.query to it.city }.reactIn(scope) { (query, city) ->
            osmJob?.cancel()
            queryMessage.clear()
            val locations = api.location.searchLocations(query, city?.takeIf { it.isNotBlank() })
                .toDataOr(toaster) { return@reactIn }
            state.set { copy(locations = locations) }
        }

        state.tapOf{ it.edit }.reactIn(scope) { edit ->
            val edit = edit ?: return@reactIn
            editor.editField.update { edit.mergeLeft(it) }
            editor.readWebsite()
        }
    }

    /** Searches OpenStreetMap for the query, within the city or near the galaxy. */
    fun queryOSM() {
        val query = stateNow.query
        if (query.isBlank()) {
            queryMessage.deliver("First you have to type something.")
            return
        }
        queryMessage.deliverSending("Searching OSM...")
        osmJob?.cancel()
        osmJob = scope.launch {
            val city = stateNow.city?.takeIf { it.isNotBlank() }
            val bounds = galaxy?.geoRect.takeIf { city == null }?.scaleBy(5f)
            val locations = osm.readLocations(query, stateNow.city, bounds).toDataOr(queryMessage) { return@launch }
            queryMessage.deliverSuccess("found: ${locations.size}")
            state.set { copy(locations = locations.mapNotNull { loc -> loc.toEditOrNull() }) }
        }
    }

    /** Offers the OpenStreetMap place at the center of the map. */
    fun whatIsHere() {
        scope.launch(::whatIsHere) {
            val center = map.geoMap.camera.centerState.now
            osm.readLocationAt(center).toDataOr(mapMessage) { return@launch }.let { location ->
                val location = location.toEditOrNull() ?: return@launch
                mapMessage.deliver(location.label)
                state.set { copy(locations = listOf(location))}
            }
        }
    }

    /** Starts a new location named with the query, at the center of the map. */
    fun createLocation() {
        state.set { copy(edit = LocationEdit(name = stateNow.query, geoPoint = map.centerNow)) }
    }

    fun review() {
        if (!editor.isEditValid()) return
        state.set { copy(isReviewing = true) }
    }

    /** The chosen location, or the new one once saved. */
    suspend fun submitLocation() = when (val location = stateNow.location) {
        null -> editor.submitSuspend().also { location ->
            state.set { copy(location = location) }
        }
        else -> location
    }

    /** Saves the location when it is new, then posts it to the galaxy when there is one. */
    fun post() {
        scope.launch {
            val location = submitLocation() ?: return@launch
            val postId = galaxy?.let { galaxy ->
                val edit = PostEdit(null, galaxy.galaxyId, PostType.Location, location.locationId.value, null)
                api.post.createPost(edit).toDataOr(postMessage) { return@launch }.postId
            }
            when (postModeState.now) {
                LocationPostMode.ResetLocation -> {
                    toaster.deliverSuccess("Posted! Ready for the next one.")
                    reset()
                }
                LocationPostMode.Return -> {
                    toaster.deliverSuccess(galaxy?.let { "Posted to ${it.name}." } ?: "Posted ${location.name ?: "Location"}.")
                    state.set { copy(postId = postId, isPosted = true) }
                }
            }
        }
    }

    /** Returns to the search with nothing chosen, cancelling an OpenStreetMap search. */
    fun reset() {
        osmJob?.cancel()
        state.set { initialState }
        queryMessage.clear()
        mapMessage.clear()
        postMessage.clear()
    }
}

data class LocationScoutState(
    val query: String = "",
    val city: String? = null,
    val locations: List<LocationEntity> = emptyList(),
    val edit: LocationEdit? = null,
    val location: Location? = null,
    val isReviewing: Boolean = false,
    val postId: PostId? = null,
    val isPosted: Boolean = false,
)

enum class LocationScoutStage: Labeled {
    Search,
    Edit,
    Review;

    override val label get() = name
}

enum class SearchMode {
    Search,
    Map,
}