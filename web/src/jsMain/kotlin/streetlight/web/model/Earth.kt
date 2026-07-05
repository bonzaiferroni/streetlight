package streetlight.web.model

import kampfire.model.handleOutcome
import koala.model.MarkerFocus
import koala.model.Portal
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.web.CityMapRoute
import streetlight.web.EarthLayer
import streetlight.web.EarthMap
import streetlight.web.EarthRoute
import streetlight.web.GalaxyMap
import streetlight.web.GalaxyMapRoute
import streetlight.web.io.ApiClient

class Earth(
    private val scope: CoroutineScope,
    initialMap: EarthMap?,
    initialLayer: EarthLayer,
    private val api: ApiClient,
    private val portal: Portal,
    private val toaster: Toaster,
    private val markerService: MarkerService,
    private val markerMap: MarkerMap
) {

    private val state = storeOf(EarthMapState(initialMap, initialLayer))
    val stateFlow = state.flow
    val stateNow get() = state.now
    val mapFlow = stateFlow.mapDistinct { it.map }
    val boundedMarkersFlow = markerMap.boundedMarkersFlow.mapDistinct { it ?: emptyList() }
    val unboundedMarkersFlow = markerMap.unboundedMarkersFlow.mapDistinct { it ?: emptyList() }
    val summaryFlow = markerMap.boundedMarkersFlow.mapDistinct { points ->
        points?.groupingBy { it.markerType }?.eachCount()?.toList()
    }
    val isMovingFlow = markerMap.isMovingFlow
    val focusFlow = markerMap.focusFlow.mapDistinct { focus ->
        when (val galaxy = ((focus as? MarkerFocus)?.marker as? GalaxyMarker)?.galaxy) {
            null -> focus
            else -> {
                portal.go(GalaxyMapRoute(galaxy.slug))
                null
            }
        }
    }
    val isFocusedFlow = focusFlow.mapDistinct { it != null }

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
                        val points = api.readTopGalaxies().handleOutcome(toaster::toast)?.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(points)
                        state.set { it.copy(map = null, layer = EarthLayer.Galaxy) }
                    }

                    else -> {
                        val galaxy = api.readGalaxy(slug).handleOutcome(toaster::toast)
                        if (galaxy == null) {
                            toaster.toast("galaxy not found: $slug")
                            return
                        }

                        val points = api.readPosts(galaxy.galaxyId).handleOutcome(toaster::toast)?.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(points)
                        state.set { it.copy(map = GalaxyMap(galaxy), layer = EarthLayer.Galaxy) }
                    }
                }
            }

            is CityMapRoute -> {
                console.log("yer city map") // ey
            }
        }
    }
}

data class EarthMapState(
    val map: EarthMap?,
    val layer: EarthLayer,
)

// val maps: List<EarthMap> = emptyList(),
