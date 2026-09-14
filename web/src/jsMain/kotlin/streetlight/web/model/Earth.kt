package streetlight.web.model

import kampfire.model.toDataOr
import koala.utils.launch
import koala.model.FeatureMarker
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
import streetlight.model.ui.ResultMap
import streetlight.model.ui.ResultMapRoute
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class Earth(
    private val scope: CoroutineScope,
    initialMap: EarthMap?,
    private val api: ApiClient,
    private val portal: Portal,
    private val toaster: Toaster,
    private val markerService: MarkerService,
    private val markerMap: MarkerMap
) {
    private val state = storeOf(EarthMapState(initialMap))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val mapState = state.tapOf { it.map }
    val boundedMarkersState = markerMap.partitionedField.tapOf { it?.bounded ?: emptyList() }
    val unboundedMarkersState = markerMap.partitionedField.tapOf { it?.unbounded ?: emptyList() }
    val summaryField = boundedMarkersState.tapOf { points ->
        points.groupingBy { it.typeLabel }.eachCount().toList()
    }
    val isMovingState = markerMap.isMovingField
    val focusState = markerMap.focusField.tapOf { focus -> focus?.takeIf { it.toGalaxy() == null } }
    val isFocusedState = focusState.tapOf { it != null }

    init {
        scope.launch("Earth > routeFlowOf") {
            portal.routeFlowOf<EarthRoute>(false).collect {
                launch {
                    collectRoute(it)
                }
            }
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
        val map = createMarkers(route) ?: return
        state.set { copy(map = map) }
        delay(10.milliseconds)
        showAll()
    }

    private suspend fun createMarkers(route: EarthRoute): EarthMap? {
        return when (route) {
            is GalaxyMapRoute -> {
                when (val slug = route.slug) {
                    null -> {
                        // td: replace with map bounds as argument
                        val markers = api.readTopGalaxies().toDataOr(toaster) { return null }.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        GalaxyMap(null)
                    }

                    else -> {
                        val galaxy = api.readGalaxy(slug).toDataOr(toaster) { return null }

                        val markers = api.readPosts(galaxy.galaxyId).toDataOr(toaster) { return null }.let {
                            markerService.createMarkers(it.entities)
                        }
                        markerMap.setPoints(markers)
                        GalaxyMap(galaxy)
                    }
                }
            }

            is CityMapRoute -> {
                when (val slug = route.slug) {
                    null -> {
                        val markers = api.readTopCities().toDataOr(toaster) { return null }.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        CityMap(null)
                    }
                    else -> {
                        val city = api.readCity(slug).toDataOr(toaster) { return null }

                        val markers = api.readCityPosts(slug).toDataOr(toaster) { return null }.let {
                            markerService.createMarkers(it)
                        }
                        markerMap.setPoints(markers)
                        CityMap(city)
                    }
                }
            }

            is ResultMapRoute -> {
                delay(1.seconds)
                val camera = markerMap.geoMap.camera
                while (camera.stateNow.isMoving) {
                    delay(100.milliseconds)
                }

                if (markerMap.stateNow.markers.isNullOrEmpty()) {
                    val bounds = camera.stateNow.bounds
                    println(bounds)
                    val markers = api.readPostsInBounds(bounds).toDataOr(toaster) { return null }.let {
                        markerService.createMarkers(it)
                    }
                    println("markers: ${markers.size}")
                    markerMap.setPoints(markers)
                }
                ResultMap(route.title)
            }
        }
    }

    private fun GeoFocus.toGalaxy() = ((this as? MarkerFocus)?.marker as? GalaxyMarker)?.galaxy
}

data class EarthMapState(
    val map: EarthMap?,
)

// val maps: List<EarthMap> = emptyList(),
