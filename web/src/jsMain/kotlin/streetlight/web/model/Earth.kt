@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.reactIn
import kampfire.model.UIMessageType
import kampfire.model.toDataOr
import koala.utils.launch
import koala.model.StaticMarker
import koala.model.Portal
import kampfire.model.tapOf
import kampfire.model.storeOf
import koala.model.PointMarker
import koala.model.readCurrentLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.Entity
import streetlight.model.data.EventTag
import streetlight.model.ui.CityMap
import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.CityRoute
import streetlight.model.ui.EarthMap
import streetlight.model.ui.EarthRoute
import streetlight.model.ui.GalaxyMap
import streetlight.model.ui.GalaxyMapRoute
import streetlight.model.ui.EventsMap
import streetlight.model.ui.EventsMapRoute
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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

    val tagState = portal.routeState.tapOf { (it as? EventsMapRoute)?.tag }
    val searchTextState = storeOf((portal.stateNow.route as? EventsMapRoute)?.searchText.orEmpty())

    val cache = EarthCache(scope, api, markerMap, toaster)
    val isQueryingState = cache.isQueryingState

    private var inflateFocusJob: Job? = null
    private var issuedRoute: EventsMapRoute? = null

    init {
        scope.launch("Earth > routeFlowOf") {
            portal.routeFlowOf<EarthRoute>(false).collect {
                launch {
                    collectRoute(it)
                }
            }
        }

        markerMap.focusState.reactIn(scope) { focus ->
            reactToFocus(focus)
        }

        scope.launch("Earth > search") {
            searchTextState.flow.debounce(1.seconds).collect {
                goToSearch()
            }
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
        state.set { copy(map = map) }
        when (route) {
            is EventsMapRoute -> {
                cache.setMapContext(true, route.tag, route.searchText)
                showAllWhenNoneInView()
            }
            else -> cache.setMapContext(false)
        }
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
                if (route != issuedRoute) {
                    searchTextState.set(route.searchText.orEmpty())
                }
                EventsMap(route.title)
            }
        }
    }

    /** Shows the events carrying [tag], or those of every tag without one. */
    fun setTag(tag: EventTag?) {
        val current = portal.stateNow.route as? EventsMapRoute ?: return
        if (current.tag == tag) return
        go(EventsMapRoute(null, tag, current.searchText))
    }

    /** Pans the map to the device's current location, or reports that it is unavailable. */
    fun panToCurrentLocation() {
        scope.launch("Earth > current location") {
            val point = readCurrentLocation() ?: run {
                toaster.toast("Your location is unavailable", UIMessageType.Error)
                return@launch
            }
            markerMap.geoMap.camera.panMap(point)
        }
    }

    /** Navigates to the events matching the search text, unless the current route already carries it. */
    private fun goToSearch() {
        val current = portal.stateNow.route as? EventsMapRoute ?: return
        val search = searchTextState.now.trim().takeIf { it.isNotEmpty() }
        if (current.searchText == search) return
        go(EventsMapRoute(null, current.tag, search))
    }

    /** Replaces the current route with [route], as a change of filter that the backstack does not keep. */
    private fun go(route: EventsMapRoute) {
        issuedRoute = route
        portal.go(route, false)
    }

    /** Frames every marker when there are markers and none of them is in view. */
    private fun showAllWhenNoneInView() {
        val markers = markerMap.stateNow.markers.takeIf { !it.isNullOrEmpty() } ?: return
        val view = markerMap.geoMap.camera.stateNow.view
        if (markers.none { view.contains(it.geoPoint) }) showAll()
    }

    private fun reactToFocus(marker: PointMarker?) {
        inflateFocusJob?.cancel()
        if (marker == null) {
            state.set { copy(isInflatingFocus = false, focusEntities = null) }
            return
        }

        when (marker) {
            is CityMarker -> {
                markerMap.geoMap.setFocus(null)
                portal.go(CityRoute(marker.city.slug))
            }
            is InflateMarker -> {
                inflateFocusJob = scope.launch {
                    state.set { copy(isInflatingFocus = true) }
                    val route = portal.stateNow.route as? EventsMapRoute
                    val entities = api.earth.inflate(marker.group.locationId, route?.tag, route?.searchText)
                        .toDataOr(toaster) { return@launch }
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
