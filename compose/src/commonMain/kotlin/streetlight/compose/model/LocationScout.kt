package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.Ok
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
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
    val queryMessageState = store.tapOf { it.queryMessage }
    val mapMessageState = store.tapOf { it.mapMessage }

    init {
        viewModelScope.launch {
            store.flow.map { it.query to it.city }.distinctUntilChanged().collectLatest { (query, city) ->
                osmJob?.cancel()
                store.set { copy(queryMessage = null) }
                val locations = when {
                    query.isBlank() -> emptyList()
                    else -> api.location.searchLocations(query, city.takeIf { it.isNotBlank() })
                        .toDataOr { return@collectLatest }
                }
                setLocations(locations)
            }
        }
    }

    /** Searches OpenStreetMap for the query, within the city or near the galaxy. */
    fun queryOsm() {
        val state = store.now
        if (state.query.isBlank()) return store.set { copy(queryMessage = "First you have to type something.") }
        store.set { copy(queryMessage = "Searching OSM...") }
        osmJob?.cancel()
        osmJob = viewModelScope.launch {
            val city = state.city.takeIf { it.isNotBlank() }
            val bounds = galaxy?.geoRect.takeIf { city == null }?.scaleBy(OsmBoundsScale)
            val found = osm.readLocations(state.query, city, bounds)
                .toDataOr { return@launch store.set { copy(queryMessage = it.message) } }
            store.set { copy(queryMessage = "found: ${found.size}") }
            setLocations(found.mapNotNull { it.toEditOrNull() })
        }
    }

    /** Offers the OpenStreetMap place at the center of the map. */
    fun whatIsHere() {
        val center = geoMap.camera.cameraState.now.center ?: return
        viewModelScope.launch {
            val found = osm.readLocationAt(center).toDataOr { return@launch store.set { copy(mapMessage = it.message) } }
            val location = found.toEditOrNull() ?: return@launch
            store.set { copy(mapMessage = location.name ?: location.address) }
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

    private fun setLocations(locations: List<LocationEntity>) {
        store.set { copy(locations = locations) }
        markerMap.setPoints(locations.filterIsInstance<Entity>())
    }
}

/** The search of a [LocationScout], what it found, the [focus] of the star's work, and the last message of each part. */
data class LocationScoutState(
    val query: String = "",
    val city: String = "",
    val locations: List<LocationEntity> = emptyList(),
    val focus: LocationScoutFocus? = null,
    val queryMessage: String? = null,
    val mapMessage: String? = null,
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
