@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.getContainingBounds
import koala.model.FeatureMarker
import koala.model.GeoFocus
import koala.model.GeoMap
import koala.model.MarkerFocus
import koala.model.dedup
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
    val markerLayer = geoMap.getOrCreateLayer(MarkerLayerConfig.Markers)

    val markersFlow = stateFlow.dedup { it.markers }
    private val partitionedFlow = markersFlow.combine(geoMap.camera.movingBoundsFlow) { markers, bounds ->
        markers?.partition { bounds.contains(it.geoPoint) }
    }.distinctUntilChanged()
    val boundedMarkersFlow   = partitionedFlow.map { it?.first }
    val unboundedMarkersFlow = partitionedFlow.map { it?.second }
    val isMovingFlow = geoMap.camera.isMovingFlow
    val focusFlow = stateFlow.dedup { it.focus }

    init {
        scope.launch {
            geoMap.focusFlow.collect { focus ->
                state.setValue { it.copy(focus = focus) }
            }
        }
    }

    fun setPoints(markers: List<FeatureMarker>?) {
        markerLayer.setPoints(markers ?: emptyList())
        state.setValue { it.copy(markers = markers, focus = null) }
    }

//    fun addPoints(markers: List<AppMarker>) {
//        geoMap.addEntities(markers)
//        state.set { it.copy(markers = (it.markers ?: emptyList()) + markers)}
//    }

    fun setFocus(marker: FeatureMarker) {
        val focus = MarkerFocus(marker)
        geoMap.setFocus(focus)
        geoMap.camera.panMap(marker.geoPoint)
        state.setValue { it.copy(focus = focus)}
    }

    fun showAll() {
        val markers = stateNow.markers.takeIf { !it.isNullOrEmpty() } ?: return
        when(val bounds = getContainingBounds(markers.map { it.geoPoint })) {
            null -> geoMap.camera.panMap(markers.first().geoPoint)
            else -> geoMap.camera.panMap(bounds.resizeBy(1.5f))
        }
    }
}

data class StreetMapState(
    val markers: List<FeatureMarker>? = null,
    val focus: GeoFocus? = null,
)
