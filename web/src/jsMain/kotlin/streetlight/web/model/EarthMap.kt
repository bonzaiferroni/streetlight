package streetlight.web.model

import kampfire.model.getDataOrNull
import kampfire.model.handleResponse
import koala.model.Portal
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.web.EarthRoute
import streetlight.web.io.ApiClient
import kotlin.collections.groupBy

class EarthMap(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val portal: Portal,
    private val toaster: Toaster,
    private val markerService: MarkerService,
    private val markerMap: MarkerMap
) {

    private val state = storeOf(EarthMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val boundedMarkersFlow = markerMap.boundedMarkersFlow.mapDistinct { it ?: emptyList() }
    val unboundedMarkersFlow = markerMap.unboundedMarkersFlow.mapDistinct { it ?: emptyList() }
    val summaryFlow = markerMap.boundedMarkersFlow.mapDistinct { points ->
        points?.groupBy { it.markerType }
    }
    val isMovingFlow = markerMap.isMovingFlow
    val focusFlow = markerMap.focusFlow

    init {
        scope.launch {
            launch {
                val galaxies = api.readTopGalaxies().getDataOrNull() ?: return@launch
                state.set { it.copy(galaxies = galaxies) }
            }
            launch {
                portal.routeFlowOf<EarthRoute>(false).collect { route ->
                    val galaxy = route.slug?.let {
                        api.readGalaxy(it).handleResponse(toaster::toast)
                    }
                    val posts = galaxy?.let {
                        api.readPosts(it.galaxyId).handleResponse(toaster::toast)
                    }
                    val points = posts?.let {
                        markerService.createEntities(it)
                    } ?: api.readTopGalaxies().handleResponse(toaster::toast)?.let {
                        markerService.createEntities(it)
                    }
                    markerMap.setPoints(points)
                    state.set { it.copy(galaxy = galaxy) }
                }
            }
        }
    }

    fun setFocus(marker: AppMarker) = markerMap.setFocus(marker)
}

data class EarthMapState(
    val galaxy: Galaxy? = null,
    val galaxies: List<Galaxy> = emptyList(),
)
