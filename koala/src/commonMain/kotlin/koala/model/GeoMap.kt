package koala.model

import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/** The map's layers and focused marker, shared by every view that shows the map. */
class GeoMap(
    private val scope: CoroutineScope,
    val camera: GeoCamera,
) {
    private val state = storeOf(GeoMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    private val layers = mutableListOf<GeoLayer>()
    private val _layersFlow = MutableSharedFlow<List<GeoLayer>>(1)
    internal val layersFlow: Flow<List<GeoLayer>> = _layersFlow

    val focusFlow = stateFlow.dedup { it.focus }

    /** The layer with the id in [config], created when there is none. */
    fun getOrCreateLayer(config: GeoLayerConfig) = layers.firstOrNull { it.layerId == config.layerId }
        ?: GeoLayer(config).also { layer ->
            layers.add(layer)
            scope.launch {
                _layersFlow.emit(layers.toList())
            }
        }

    /** Removes the layer with [layerId], if present. */
    fun removeLayer(layerId: GeoLayerId) = layers.firstOrNull { it.layerId == layerId }?.let {
        layers.remove(it)
        scope.launch {
            _layersFlow.emit(layers.toList())
        }
    }

    // fun setFocus(value: PointMarker?) = state.set { it.copy(focus = value?.let { marker -> MarkerFocus(marker)} ) }

    fun setFocus(value: GeoFocus?) = state.update { it.copy(focus = value) }
}

/** The focused marker of the map, if any. */
data class GeoMapState(
    val focus: GeoFocus? = null
)

/** The focus of the map: a single marker or a cluster. */
sealed interface GeoFocus {
    val markerId: MarkerId
}

/** Focus on a single [marker]. */
data class MarkerFocus(
    val marker: PointMarker
): GeoFocus {
    override val markerId get() = marker.markerId
}

/** Focus on a cluster, shown at its [principal] marker. */
data class ClusterFocus(
    val principal: PointMarker,
    val members: List<PointMarker>
): GeoFocus {
    override val markerId get() = principal.markerId
}