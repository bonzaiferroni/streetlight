package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.Ok
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.model.MessageStore
import koala.model.Toaster
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.compose.io.OsmClient
import streetlight.model.data.Entity
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationEntity
import streetlight.model.data.PostEdit
import streetlight.model.data.PostType
import streetlight.model.data.toEditOrNull
import streetlight.model.io.ApiClient

/**
 * Finds or starts a location, to post it to [galaxy], when given. A location is found in Streetlight as the query is
 * typed, or in OpenStreetMap on request, or put on the map where it looks. A new location is written in its [editor].
 * The focus is what the star is working on, and its stage follows from it. The cousin of the web's `LocationScout`.
 */
class LocationScout(
    val galaxy: Galaxy?,
    private val api: ApiClient,
    private val osm: OsmClient,
    private val geoMap: GeoMap,
    settings: AppSettings,
    private val toaster: Toaster,
) : ViewModel() {
    private val store = storeOf(LocationScoutState())
    private var osmJob: Job? = null

    val markerMap = MarkerMap(geoMap)
    val editor = LocationEditor(api)
    val queryState = store.mutableTapOf({ it.query }) { copy(query = it) }
    val cityState = store.mutableTapOf({ it.city }) { copy(city = it) }
    val locationsState = store.tapOf { it.locations }
    val focusState = store.tapOf { it.focus }
    val stageState = store.tapOf { it.focus.toStage() }
    val queryMessage = MessageStore()
    val mapMessage = MessageStore()
    val postMessage = MessageStore()
    val isPostedState = store.tapOf { it.isPosted }
    val postModeState = settings.locationPostModeState
    val feedModeState = settings.feedModeState

    init {
        viewModelScope.launch {
            store.flow.map { it.query to it.city }.distinctUntilChanged().collectLatest { (query, city) ->
                osmJob?.cancel()
                queryMessage.clear()
                val locations = when {
                    query.isBlank() -> emptyList()
                    else -> api.location.searchLocations(query, city.takeIf { it.isNotBlank() })
                        .toDataOr(toaster) { return@collectLatest }
                }
                setLocations(locations)
            }
        }
    }

    /** Searches OpenStreetMap for the query, within the city or near the galaxy. */
    fun queryOsm() {
        val state = store.now
        if (state.query.isBlank()) return queryMessage.deliver("First you have to type something.")
        queryMessage.deliverSending("Searching OSM...")
        osmJob?.cancel()
        osmJob = viewModelScope.launch {
            val city = state.city.takeIf { it.isNotBlank() }
            val bounds = galaxy?.geoRect.takeIf { city == null }?.scaleBy(OsmBoundsScale)
            val found = osm.readLocations(state.query, city, bounds).toDataOr(queryMessage) { return@launch }
            queryMessage.deliverSuccess("found: ${found.size}")
            setLocations(found.mapNotNull { it.toEditOrNull() })
        }
    }

    /** Offers the OpenStreetMap place at the center of the map. */
    fun whatIsHere() {
        val center = geoMap.camera.cameraState.now.center ?: return
        viewModelScope.launch {
            val found = osm.readLocationAt(center).toDataOr(mapMessage) { return@launch }
            val location = found.toEditOrNull() ?: return@launch
            mapMessage.deliver(location.label)
            setLocations(listOf(location))
        }
    }

    /** Starts a new location named with the query, at the center of the map. */
    fun createLocation() {
        val center = geoMap.camera.cameraState.now.center
        focus(LocationEdit(name = store.now.query.takeIf { it.isNotBlank() }, geoPoint = center))
    }

    /** Works on [location]: reviews one of Streetlight's, or starts editing one that is new. */
    fun focus(location: LocationEntity) {
        val focus = when (location) {
            is Location -> LocationScoutFocus.Review(location)
            is LocationEdit -> LocationScoutFocus.Edit.also { editor.setEdit(location) }
        }
        store.set { copy(focus = focus) }
    }

    /** Reviews the new location, once its edit is valid. */
    fun review() {
        if (!editor.isEditValid()) return
        store.set { copy(focus = LocationScoutFocus.Review(editor.editNow)) }
    }

    /** Returns to editing the new location, as it was left. */
    fun edit() = store.set { copy(focus = LocationScoutFocus.Edit) }

    /** Returns to the search, keeping what was found. */
    fun search() = store.set { copy(focus = null) }

    /** Saves the location when it is new, posts it to the galaxy when there is one, and goes on by the post mode. */
    fun post() {
        val focus = store.now.focus as? LocationScoutFocus.Review ?: return
        postMessage.clear()
        viewModelScope.launch {
            val location = when (val location = focus.location) {
                is Location -> location
                is LocationEdit -> editor.saveLocation()?.also {
                    store.set { copy(focus = LocationScoutFocus.Review(it)) }
                }
            } ?: return@launch
            galaxy?.let { galaxy ->
                val edit = PostEdit(null, galaxy.galaxyId, PostType.Location, location.locationId.value, null)
                api.post.createPost(edit).toDataOr(postMessage) { return@launch }
            }
            when (postModeState.now) {
                LocationPostMode.ResetLocation -> {
                    toaster.deliverSuccess("Posted! Ready for the next one.")
                    reset()
                }
                LocationPostMode.Return -> {
                    val posted = galaxy?.let { "Posted to ${it.name}." } ?: "Posted ${location.name ?: "Location"}."
                    toaster.deliverSuccess(posted)
                    store.set { copy(isPosted = true) }
                }
            }
        }
    }

    /** Returns to the search with nothing chosen or found, cancelling an OpenStreetMap search. */
    fun reset() {
        osmJob?.cancel()
        store.set { LocationScoutState() }
        editor.setEdit(LocationEdit())
        markerMap.setPoints(emptyList())
        queryMessage.clear()
        mapMessage.clear()
        postMessage.clear()
    }

    private fun setLocations(locations: List<LocationEntity>) {
        store.set { copy(locations = locations) }
        markerMap.setPoints(locations.filterIsInstance<Entity>())
    }
}

/** The search of a [LocationScout], what it found, the [focus] of the star's work, and whether it [isPosted]. */
data class LocationScoutState(
    val query: String = "",
    val city: String = "",
    val locations: List<LocationEntity> = emptyList(),
    val focus: LocationScoutFocus? = null,
    val isPosted: Boolean = false,
)

/** The location a [LocationScout] works on, past the search. */
sealed interface LocationScoutFocus {
    /** A new location, its details being written in the scout's editor. */
    data object Edit : LocationScoutFocus

    /** A [location] ready to post: one of Streetlight's, or a new one as written. */
    data class Review(val location: LocationEntity) : LocationScoutFocus
}

/** The stages of a [LocationScout], in order, each with its [label]. */
enum class LocationScoutStage(val label: String) {
    Search("Search"),
    Edit("Edit"),
    Review("Review"),
}

/** The stage of this focus: the search without one. */
fun LocationScoutFocus?.toStage() = when (this) {
    null -> LocationScoutStage.Search
    is LocationScoutFocus.Edit -> LocationScoutStage.Edit
    is LocationScoutFocus.Review -> LocationScoutStage.Review
}

/** The scale of a galaxy's bounds that an OpenStreetMap search without a city looks within, as the web's. */
private const val OsmBoundsScale = 5f
