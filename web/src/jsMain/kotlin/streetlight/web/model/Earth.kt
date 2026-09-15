package streetlight.web.model

import kampfire.model.toDataOr
import koala.utils.launch
import koala.model.EntityMarker
import koala.model.GeoFocus
import koala.model.MarkerFocus
import koala.model.Portal
import kampfire.model.tapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.ui.CityMap
import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.EarthMap
import streetlight.model.ui.EarthRoute
import streetlight.model.ui.GalaxyMap
import streetlight.model.ui.GalaxyMapRoute
import streetlight.model.ui.PostMap
import streetlight.model.ui.PostMapRoute
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds

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
    val boundedMarkersState = markerMap.viewMarkersState.tapOf { it?.bounded ?: emptyList() }
    val unboundedMarkersState = markerMap.viewMarkersState.tapOf { it?.unbounded ?: emptyList() }
    val summaryField = boundedMarkersState.tapOf { points ->
        points.groupingBy { it.typeLabel }.eachCount().toList()
    }
    val isMovingState = markerMap.isMovingState
    val focusState = markerMap.focusState.tapOf { focus -> focus?.takeIf { it.toGalaxy() == null } }
    val isFocusedState = focusState.tapOf { it != null }

    val cache = EarthCache(scope, api, markerMap, toaster)

    init {
        scope.launch("Earth > routeFlowOf") {
            portal.routeFlowOf<EarthRoute>(false).collect {
                launch {
                    collectRoute(it)
                }
            }
        }
        scope.launch("Earth > focus galaxy") {
            markerMap.focusState.flow.collect {
                it?.toGalaxy()?.let { galaxy ->
                    portal.go(GalaxyMapRoute(galaxy.slug))
                }
            }
        }
    }

    fun setFocus(marker: EntityMarker) = markerMap.setFocus(marker)

    fun showAll() = markerMap.showAll()

    private suspend fun collectRoute(route: EarthRoute) {
        val map = createMap(route) ?: return
        cache.setMapContext(route is PostMapRoute)
        state.set { copy(map = map) }
        delay(100.milliseconds)
        showAll()
    }

    private suspend fun createMap(route: EarthRoute): EarthMap? {
        return when (route) {
            is GalaxyMapRoute -> {
                when (val slug = route.slug) {
                    null -> {
                        // td: replace with map bounds as argument
                        val galaxies = api.readTopGalaxies().toDataOr(toaster) { return null }
                        markerMap.setPoints(galaxies)
                        GalaxyMap(null)
                    }

                    else -> {
                        val galaxy = api.readGalaxy(slug).toDataOr(toaster) { return null }

                        val feed = api.readPosts(galaxy.galaxyId).toDataOr(toaster) { return null }
                        markerMap.setPoints(feed.entities)
                        GalaxyMap(galaxy)
                    }
                }
            }

            is CityMapRoute -> {
                when (val slug = route.slug) {
                    null -> {
                        val cities = api.readTopCities().toDataOr(toaster) { return null }
                        markerMap.setPoints(cities)
                        CityMap(null)
                    }
                    else -> {
                        val city = api.readCity(slug).toDataOr(toaster) { return null }

                        val posts = api.readCityPosts(slug).toDataOr(toaster) { return null }
                        markerMap.setPoints(posts)
                        CityMap(city)
                    }
                }
            }

            is PostMapRoute -> {
                markerMap.filterPoints {
                    when (it) {
                        is LocationMarker, is EventMarker, is MediaMarker -> true
                        else -> false
                    }
                }
                PostMap(route.title)
            }
        }
    }

    private fun GeoFocus.toGalaxy() = ((this as? MarkerFocus)?.marker as? GalaxyMarker)?.galaxy
}

data class EarthMapState(
    val map: EarthMap?,
)

// val maps: List<EarthMap> = emptyList(),
