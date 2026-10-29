package streetlight.compose.model

import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.model.MessageStore
import koala.model.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.compose.io.OsmClient
import streetlight.model.data.Entity
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationEntity
import streetlight.model.data.toEditOrNull
import streetlight.model.io.ApiClient

/**
 * The search for a location near [galaxy], when given: in Streetlight as the query is typed, in OpenStreetMap on
 * request, or on the map where it looks. What the star picks or starts goes to [onFind]. A submodel, held by the view
 * model of any view that finds a location, as `LocationScout` and `EventScout` do.
 */
class LocationFinder(
    private val scope: CoroutineScope,
    val galaxy: Galaxy?,
    private val api: ApiClient,
    private val osm: OsmClient,
    private val geoMap: GeoMap,
    private val toaster: Toaster,
    private val onFind: (LocationEntity) -> Unit,
) {
    private val store = storeOf(LocationFinderState())
    private var osmJob: Job? = null

    val markerMap = MarkerMap(geoMap)
    val queryState = store.mutableTapOf({ it.query }) { copy(query = it) }
    val cityState = store.mutableTapOf({ it.city }) { copy(city = it) }
    val locationsState = store.tapOf { it.locations }
    val queryMessage = MessageStore()
    val mapMessage = MessageStore()

    init {
        scope.launch {
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
        osmJob = scope.launch {
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
        scope.launch {
            val found = osm.readLocationAt(center).toDataOr(mapMessage) { return@launch }
            val location = found.toEditOrNull() ?: return@launch
            mapMessage.deliver(location.label)
            setLocations(listOf(location))
        }
    }

    fun pick(location: LocationEntity) = onFind(location)

    /** Starts a new location named with the query, at the center of the map. */
    fun createLocation() {
        val center = geoMap.camera.cameraState.now.center
        onFind(LocationEdit(name = store.now.query.takeIf { it.isNotBlank() }, geoPoint = center))
    }

    /** Clears the search and what it found, cancelling an OpenStreetMap search. */
    fun reset() {
        osmJob?.cancel()
        store.set { LocationFinderState() }
        markerMap.setPoints(emptyList())
        queryMessage.clear()
        mapMessage.clear()
    }

    private fun setLocations(locations: List<LocationEntity>) {
        store.set { copy(locations = locations) }
        markerMap.setPoints(locations.filterIsInstance<Entity>())
    }
}

data class LocationFinderState(
    val query: String = "",
    val city: String = "",
    val locations: List<LocationEntity> = emptyList(),
)

/** The scale of a galaxy's bounds that an OpenStreetMap search without a city looks within, as the web's. */
private const val OsmBoundsScale = 5f
