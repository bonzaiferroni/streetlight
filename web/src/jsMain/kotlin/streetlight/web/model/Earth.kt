package streetlight.web.model

import kampfire.model.toDataOr
import koala.utils.launch
import koala.model.FeatureMarker
import koala.model.GeoFocus
import koala.model.MarkerFocus
import koala.model.Portal
import koala.model.tapOf
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
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

    val mapField = state.tapOf { it.map }
    val boundedMarkersField = markerMap.partitionedField.tapOf { it?.bounded ?: emptyList() }
    val unboundedMarkersField = markerMap.partitionedField.tapOf { it?.unbounded ?: emptyList() }
    val summaryField = boundedMarkersField.tapOf { points ->
        points.groupingBy { it.typeLabel }.eachCount().toList()
    }
    val isMovingField = markerMap.isMovingField
    val focusField = markerMap.focusField.tapOf { focus -> focus?.takeIf { it.toGalaxy() == null } }
    val isFocusedField = focusField.tapOf { it != null }

    init {
        scope.launch("Earth > routeFlowOf") {
            portal.routeFlowOf<EarthRoute>(false).collect(::collectRoute)
        }
        scope.launch("Earth > focus galaxy") {
            markerMap.focusField.flow.collect {
                it?.toGalaxy()?.let { galaxy ->
                    portal.go(GalaxyMapRoute(galaxy.slug))
                }
            }
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
                        val markers = api.readTopGalaxies().toDataOr(toaster) { return }.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        state.set { copy(map = GalaxyMap(null)) }
                        showAll()
                    }

                    else -> {
                        val galaxy = api.readGalaxy(slug).toDataOr(toaster) { return }

                        val markers = api.readPosts(galaxy.galaxyId).toDataOr(toaster) { return }.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        state.set { copy(map = GalaxyMap(galaxy)) }
                        showAll()
                    }
                }
            }

            is CityMapRoute -> {
                when (val slug = route.slug) {
                    null -> {
                        val markers = api.readTopCities().toDataOr(toaster) { return }.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        state.set { copy(map = CityMap(null))}
                        showAll()
                    }
                    else -> {
                        val city = api.readCity(slug).toDataOr(toaster) { return }

                        val markers = api.readCityPosts(slug).toDataOr(toaster) { return }.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        state.set { copy(map = CityMap(city)) }
                        showAll()
                    }
                }
            }
        }
    }

    private fun GeoFocus.toGalaxy() = ((this as? MarkerFocus)?.marker as? GalaxyMarker)?.galaxy
}

data class EarthMapState(
    val map: EarthMap,
)

// val maps: List<EarthMap> = emptyList(),
