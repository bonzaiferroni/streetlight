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
import kotlinx.coroutines.launch

class MarkerMap(
    private val scope: CoroutineScope,
    private val cache: DataCache,
    private val geoMap: GeoMap,
) {
    private val state = storeOf(StreetMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    val markersFlow = stateFlow.mapDistinct { it.markers }
    private val partitionedFlow = markersFlow.combine(geoMap.movingBoundsFlow) { markers, bounds ->
        markers?.partition { bounds.contains(it.geoPoint) }
    }.distinctUntilChanged()
    val boundedMarkersFlow   = partitionedFlow.map { it?.first }
    val unboundedMarkersFlow = partitionedFlow.map { it?.second }
    val isMovingFlow = geoMap.isMovingFlow
    val focusFlow = stateFlow.mapDistinct { it.focus }

    init {
        scope.launch {
            geoMap.focusFlow.collect { focus ->
                val focus = focus as? AppMarker
                state.set { it.copy(focus = focus) }
            }
        }
    }

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

    fun setFocus(marker: AppMarker) {
        geoMap.setFocus(marker)
        geoMap.panMap(marker.geoPoint)
        state.set { it.copy(focus = marker)}
    }
}

data class StreetMapState(
    val markers: List<AppMarker>? = null,
    val focus: AppMarker? = null,
)
