@file:OptIn(FlowPreview::class)

package streetlight.web.model

import koala.model.GeoMap
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MarkerMap(
    private val scope: CoroutineScope,
    private val cache: DataCache,
    private val geoMap: GeoMap,
) {
    private val state = storeOf(StreetMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    val markersFlow = stateFlow.mapDistinct { it.markers }
    private val partitionedFlow = markersFlow.combine(geoMap.boundsFlow) { markers, bounds ->
        markers?.partition { bounds.contains(it.geoPoint) }
    }.distinctUntilChanged()
    val boundedMarkersFlow   = partitionedFlow.map { it?.first }
    val unboundedMarkersFlow = partitionedFlow.map { it?.second }
    val isMovingFlow = geoMap.isMovingFlow

    fun setPoints(markers: List<AppMarker>?) {
        stateNow.markers?.let { markersNow ->
            geoMap.removeEntities(markersNow.map { it.markerId })
        }
        markers?.let {
            geoMap.addEntities(markers)
        }
        state.set { it.copy(markers = markers) }
    }

    fun addPoints(markers: List<AppMarker>) {
        geoMap.addEntities(markers)
        state.set { it.copy(markers = (it.markers ?: emptyList()) + markers)}
    }
}

data class StreetMapState(
    val markers: List<AppMarker>? = null,
    val focus: AppMarker? = null,
)
