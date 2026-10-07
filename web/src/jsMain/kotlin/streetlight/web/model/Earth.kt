package streetlight.web.model

import kampfire.model.reactIn
import kampfire.model.toDataOr
import koala.utils.launch
import koala.model.StaticMarker
import koala.model.Portal
import kampfire.model.tapOf
import kampfire.model.storeOf
import koala.model.PointMarker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.Entity
import streetlight.model.data.LocationId
import streetlight.model.ui.CityMap
import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.EarthMap
import streetlight.model.ui.EarthRoute
import streetlight.model.ui.GalaxyMap
import streetlight.model.ui.GalaxyMapRoute
import streetlight.model.ui.EventsMap
import streetlight.model.ui.EventsMapRoute
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds

/**
 * The model of the map screens: it loads the markers of each [EarthRoute] and follows a focus on a galaxy to its
 * map.
 */
class Earth(
    private val scope: CoroutineScope,
    initialMap: EarthMap?,
    private val api: ApiClient,
    private val portal: Portal,
    private val toaster: Toaster,
    private val markerMap: MarkerMap
) {
    private val state = storeOf(EarthMapState(initialMap))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val mapState = state.tapOf { it.map }
    val isMovingState = markerMap.isMovingState
    val focusEntitiesState = state.tapOf { it.focusEntities }
    val isFocusedState = focusEntitiesState.tapOf { it != null }
    val isInflatingState = state.tapOf { it.isInflatingFocus }

    val cache = EarthCache(scope, api, markerMap, toaster)
    val isQueryingState = cache.isQueryingState

    private var inflateFocusJob: Job? = null

    init {
        scope.launch("Earth > routeFlowOf") {
            portal.routeFlowOf<EarthRoute>(false).collect {
                launch {
                    collectRoute(it)
                }
            }
        }

        markerMap.focusState.reactIn(scope) { focus ->
            inflateFocus(focus)
        }
    }

    fun setFocus(marker: StaticMarker) = markerMap.setFocus(marker)

    /** Frames every marker. */
    fun showAll() = markerMap.showAll()

    private suspend fun collectRoute(route: EarthRoute) {
        val map = createMap(route) ?: return
        route.bounds?.let {
            markerMap.geoMap.camera.panMap(it)
        }
        cache.setMapContext(route is EventsMapRoute)
        state.set { copy(map = map) }
        delay(100.milliseconds)
        // showAll()
    }

    private suspend fun createMap(route: EarthRoute): EarthMap? {
        return when (route) {
            is GalaxyMapRoute -> {
                when (val slug = route.slug) {
                    null -> {
                        // td: replace with map bounds as argument
                        val galaxies = api.galaxy.readTopGalaxies().toDataOr(toaster) { return null }
                        markerMap.setPoints(galaxies)
                        GalaxyMap(null)
                    }

                    else -> {
                        val galaxy = api.galaxy.readGalaxy(slug).toDataOr(toaster) { return null }

                        val feed = api.post.readPosts(galaxy.galaxyId).toDataOr(toaster) { return null }
                        markerMap.setPoints(feed.entities)
                        GalaxyMap(galaxy)
                    }
                }
            }

            is CityMapRoute -> {
                val cities = api.city.readTopCities().toDataOr(toaster) { return null }
                markerMap.setPoints(cities)
                CityMap
            }

            is EventsMapRoute -> {
                markerMap.filterPoints { false }
                EventsMap(route.title)
            }
        }
    }

    private fun inflateFocus(marker: PointMarker?) {
        inflateFocusJob?.cancel()
        if (marker == null) {
            state.set { copy(isInflatingFocus = false, focusEntities = null) }
            return
        }

        when (marker) {
            is InflateMarker -> {
                inflateFocusJob = scope.launch {
                    state.set { copy(isInflatingFocus = true) }
                    val entities = api.earth.inflate(marker.group.locationId).toDataOr(toaster) { return@launch }
                    state.set { copy(isInflatingFocus = false, focusEntities = entities)}
                }
            }
            is EntityMarker -> {
                state.set { copy(isInflatingFocus = false, focusEntities = listOf(marker.entity)) }
            }
        }
    }
}

data class EarthMapState(
    val map: EarthMap?,
    val focusEntities: List<Entity>? = null,
    val isInflatingFocus: Boolean = false,
)

// val maps: List<EarthMap> = emptyList(),
