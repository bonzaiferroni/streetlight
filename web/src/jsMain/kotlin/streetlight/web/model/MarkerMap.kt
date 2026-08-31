@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.getContainingBounds
import koala.model.FeatureMarker
import koala.model.GeoFocus
import koala.model.GeoMap
import koala.model.MarkerFocus
import kampfire.model.combine
import kampfire.model.tapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

class MarkerMap(
    private val scope: CoroutineScope,
    val geoMap: GeoMap,
) {
    private val state = storeOf(StreetMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val markerLayer = geoMap.getOrCreateLayer(MarkerLayerConfig.Markers)
    val centerNow get() = geoMap.camera.stateNow.center

    // val markersFlow = stateFlow.dedup { it.markers }
    // private val partitionedFlow = markersFlow.combine(geoMap.camera.movingBoundsFlow) { markers, bounds ->
    //     markers?.partition { bounds.contains(it.geoPoint) }
    // }.distinctUntilChanged()
    // val boundedMarkersFlow   = partitionedFlow.map { it?.first }
    // val unboundedMarkersFlow = partitionedFlow.map { it?.second }

    val markersField = state.tapOf { it.markers }
    val partitionedField = markersField.combine(geoMap.camera.movingBoundsField) { markers, bounds ->
        markers?.partition { bounds.contains(it.geoPoint) }?.let {
            PartitionedMarkers(
                bounded = it.first,
                unbounded = it.second
            )
        }
    }
    val isMovingField = geoMap.camera.isMovingField
    val focusField = state.tapOf { it.focus }

    init {
        scope.launch {
            geoMap.focusFlow.collect { focus ->
                state.set { copy(focus = focus) }
            }
        }
    }

    fun setPoints(markers: List<FeatureMarker>?) {
        markerLayer.setPoints(markers ?: emptyList())
        state.set { copy(markers = markers, focus = null) }
    }

//    fun addPoints(markers: List<AppMarker>) {
//        geoMap.addEntities(markers)
//        state.set { it.copy(markers = (it.markers ?: emptyList()) + markers)}
//    }

    fun setFocus(marker: FeatureMarker) {
        val focus = MarkerFocus(marker)
        geoMap.setFocus(focus)
        geoMap.camera.panMap(marker.geoPoint)
        state.set { copy(focus = focus)}
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

data class PartitionedMarkers(
    val bounded: List<FeatureMarker>,
    val unbounded: List<FeatureMarker>,
)