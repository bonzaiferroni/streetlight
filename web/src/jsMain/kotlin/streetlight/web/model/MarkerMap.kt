@file:OptIn(FlowPreview::class)

package streetlight.web.model

import koala.model.GeoMap
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class MarkerMap(
    private val scope: CoroutineScope,
    private val cache: DataCache,
    private val geoMap: GeoMap,
) {
    private val state = storeOf(StreetMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    val pointsFlow = stateFlow.mapDistinct { it.points }
    val boundedPointsFlow = pointsFlow.combine(geoMap.boundsFlow) { points, bounds ->
        points?.filter { bounds.contains(it.geoPoint) }
    }.distinctUntilChanged()
    val isMovingFlow = geoMap.isMovingFlow

    fun setPoints(points: List<AppMarker>?) {
        stateNow.points?.let { pointsNow ->
            geoMap.removeEntities(pointsNow.map { it.markerId })
        }
        points?.let {
            geoMap.addEntities(points)
        }
        state.set { it.copy(points = points) }
    }

    fun addPoints(points: List<AppMarker>) {
        geoMap.addEntities(points)
        state.set { it.copy(points = (it.points ?: emptyList()) + points)}
    }
}

data class StreetMapState(
    val points: List<AppMarker>? = null,
    val focus: AppMarker? = null,
)
