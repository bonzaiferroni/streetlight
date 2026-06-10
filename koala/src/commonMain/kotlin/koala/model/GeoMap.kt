package koala.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

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

    val focusFlow = stateFlow.mapDistinct { it.focus }

    fun getOrCreateLayer(config: GeoLayerConfig) = layers.firstOrNull { it.layerId == config.layerId }
        ?: GeoLayer(config).also { layer ->
            layers.add(layer)
            scope.launch {
                _layersFlow.emit(layers.toList())
            }
        }

    fun removeLayer(layerId: GeoLayerId) = layers.firstOrNull { it.layerId == layerId }?.let {
        layers.remove(it)
        scope.launch {
            _layersFlow.emit(layers.toList())
        }
    }

    fun setFocus(value: PointMarker?) = state.set { it.copy(focus = value) }
}

data class GeoMapState(
    val focus: PointMarker? = null
)