package streetlight.web.model

import kampfire.model.handleResponse
import koala.model.FeatureMarker
import koala.model.MarkerFocus
import koala.model.Portal
import koala.model.tap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.ui.CityMap
import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.EarthMap
import streetlight.model.ui.EarthRoute
import streetlight.model.ui.GalaxyMap
import streetlight.model.ui.GalaxyMapRoute
import streetlight.web.io.ApiClient

class Earth(
    private val scope: CoroutineScope,
    initialMap: EarthMap,
    private val api: ApiClient,
    private val portal: Portal,
    private val toaster: Toaster,
    private val markerService: MarkerService,
    private val markerMap: MarkerMap
) {

    private val state = storeOf(EarthMapState(initialMap))
    val stateFlow = state.flow
    val stateNow get() = state.now
    val mapFlow = stateFlow.tap { it.map }
    val boundedMarkersFlow = markerMap.boundedMarkersFlow.tap { it ?: emptyList() }
    val unboundedMarkersFlow = markerMap.unboundedMarkersFlow.tap { it ?: emptyList() }
    val summaryFlow = markerMap.boundedMarkersFlow.tap { points ->
        points?.groupingBy { it.typeLabel }?.eachCount()?.toList()
    }
    val isMovingFlow = markerMap.isMovingFlow
    val focusFlow = markerMap.focusFlow.tap { focus ->
        when (val galaxy = ((focus as? MarkerFocus)?.marker as? GalaxyMarker)?.galaxy) {
            null -> focus
            else -> {
                portal.go(GalaxyMapRoute(galaxy.slug))
                null
            }
        }
    }
    val isFocusedFlow = focusFlow.tap { it != null }

    init {
        scope.launch {
            portal.routeFlowOf<EarthRoute>(false).collect(::collectRoute)
        }
    }

    fun setFocus(marker: FeatureMarker) = markerMap.setFocus(marker)

    fun showAll() = markerMap.showAll()

    private suspend fun collectRoute(route: EarthRoute) {
        when (route) {
            is GalaxyMapRoute -> {
                when (val slug = route.slug) {
                    null -> {
                        // td: replace with map bounds as argument
                        // val maps = api.readTopGalaxies().handleOutcome(toaster::toast)
                        //     ?.map { GalaxyMap(it) } ?: emptyList()
                        // state.set { it.copy(maps = maps) }
                        val markers = api.readTopGalaxies().handleResponse(toaster)?.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        state.set { it.copy(map = GalaxyMap(null)) }
                        showAll()
                    }

                    else -> {
                        val galaxy = api.readGalaxy(slug).handleResponse(toaster)
                        if (galaxy == null) {
                            toaster.toast("galaxy not found: $slug")
                            return
                        }

                        val markers = api.readPosts(galaxy.galaxyId).handleResponse(toaster)?.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        state.set { it.copy(map = GalaxyMap(galaxy)) }
                        showAll()
                    }
                }
            }

            is CityMapRoute -> {
                when (val slug = route.slug) {
                    null -> {
                        val markers = api.readTopCities().handleResponse(toaster)?.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        state.set { it.copy(map = CityMap(null))}
                        showAll()
                    }
                    else -> {
                        val city = api.readCity(slug).handleResponse(toaster)
                        if (city == null) {
                            toaster.toast("city not found: $slug")
                            return
                        }

                        val markers = api.readCityPosts(slug).handleResponse(toaster)?.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        state.set{ it.copy(map = CityMap(city)) }
                        showAll()
                    }
                }
            }
        }
    }
}

data class EarthMapState(
    val map: EarthMap,
)

// val maps: List<EarthMap> = emptyList(),
