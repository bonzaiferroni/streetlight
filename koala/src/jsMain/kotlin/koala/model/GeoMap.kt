package koala.model

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.Url
import koala.Svg
import koala.css.ModifierSet
import koala.dom.RenderContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.html.DIV

class GeoMap(
    private val scope: CoroutineScope
) {
    private val state = storeOf(GeoMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    private val _markerFlow = MutableSharedFlow<List<MapMarker>>(1)
    val markerFlow: SharedFlow<List<MapMarker>> = _markerFlow
    private val _removeMarker = MutableSharedFlow<List<MapMarkerId>>(1)
    val removeMarker: SharedFlow<List<MapMarkerId>> = _removeMarker
    private val _linesFlow = MutableSharedFlow<List<LineMarker>>(1)
    val linesFlow: SharedFlow<List<LineMarker>> = _linesFlow
    private val _panFlow = MutableSharedFlow<PanPoint>(1)
    val panFlow: SharedFlow<PanPoint> = _panFlow
    private val _panBoundsFlow = MutableSharedFlow<GeoBounds>(1)
    val panBoundsFlow: SharedFlow<GeoBounds> = _panBoundsFlow
    private val _markerVisibilityFlow = MutableSharedFlow<(MapMarker) -> Boolean>(1)
    val markerVisibilityFlow: SharedFlow<(MapMarker) -> Boolean> = _markerVisibilityFlow
    private val _movementFlow = MutableSharedFlow<MarkerMovement>(1)
    val movementFlow: Flow<MarkerMovement> = _movementFlow
    private val _tempEntityFlow = MutableSharedFlow<TempEntitySet?>(1)
    val tempEntityFlow: Flow<TempEntitySet?> = _tempEntityFlow
    private val _hideLayersFlow = MutableSharedFlow<List<LayerId>>()
    val hideLayersFlow: Flow<List<LayerId>> = _hideLayersFlow
    private val _showLayersFlow = MutableSharedFlow<List<LayerId>>()
    val showLayersFlow: Flow<List<LayerId>> = _showLayersFlow

    val viewedStateFlow = stateFlow.filter { it.isViewed }
    val settledStateFlow = viewedStateFlow.filter { !it.isMoving }
    val zoomFlow = settledStateFlow.mapDistinct { it.zoom }
    val centerFlow = settledStateFlow.mapDistinct { it.center }
    val boundsFlow = settledStateFlow.mapDistinct { it.bounds }

    fun addEntity(entity: MapMarker) {
        addEntities(listOf(entity))
    }

    fun addEntities(entities: List<MapMarker>) {
        scope.launch {
            _markerFlow.emit(entities)
        }
    }

    fun moveEntity(entityId: MapMarkerId, position: GeoPoint) {
        scope.launch {
            _movementFlow.emit(MarkerMovement(entityId, position))
        }
    }

    fun removeEntities(entityIds: List<MapMarkerId>) {
        scope.launch {
            _removeMarker.emit(entityIds)
        }
    }

    fun addLines(entities: List<LineMarker>) {
        scope.launch {
            _linesFlow.emit(entities)
        }
    }

    fun setEntityVisibility(filter: (MapMarker) -> Boolean) {
        scope.launch {
            _markerVisibilityFlow.emit(filter)
        }
    }

    fun setFocus(entity: PointMarker?) {
        state.set { it.copy(focus = entity) }
    }

    fun setBounds(center: GeoPoint, bounds: GeoBounds, zoom: Float, isMoving: Boolean) {
        state.set { it.copy(center = center, bounds = bounds, zoom = zoom, isMoving = isMoving) }
    }

    fun panMap(point: GeoPoint) {
        panMap(PanPoint(point))
    }

    fun panMap(bounds: GeoBounds) {
        scope.launch {
            _panBoundsFlow.emit(bounds)
        }
    }

    fun panMap(pan: PanPoint) {
        scope.launch {
            _panFlow.emit(pan)
        }
    }

    fun setIsViewed(value: Boolean) {
        state.set { it.copy(isViewed = value) }
    }

    fun tempEntities(entities: List<MapMarker>?) {
        scope.launch {
            _tempEntityFlow.emit(entities?.let { TempEntitySet(it)})
        }
    }

    fun hideLayers(layerIds: List<LayerId>) {
        scope.launch {
            _hideLayersFlow.emit(layerIds)
        }
    }

    fun showLayers(layerIds: List<LayerId>) {
        scope.launch {
            _showLayersFlow.emit(layerIds)
        }
    }
}

data class GeoMapState(
    val center: GeoPoint = GeoPoint.Denver,
    val bounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val isMoving: Boolean = false,
    val isViewed: Boolean = false,
    val focus: PointMarker? = null,
)

typealias MapMarkerId = String
typealias MapContextId = String

sealed interface MapMarker {
    val markerId: MapMarkerId
    val label: String? get() = null
}

data class PanPoint(
    val point: GeoPoint,
    val zoom: Float? = null,
    val snap: Boolean = false,
)

interface PointMarker: MapMarker {
    val geoPoint: GeoPoint
    val bearing: Float? get() = null
    val opacity: Float? get() = null
    val subpixelPositioning: Boolean get() = true
    val icon: Svg? get() = null
    val thumbUrl: Url? get() = null
    val minZoom: Float? get() = null
    val modifiers: ModifierSet? get() = null
    val onFocus: OnFocus? get() = null
    val focusContent: (RenderContext.() -> Unit)? get() = null
    val body: (DIV.() -> Unit)? get() = null
    val light: Rgb? get() = null
}

data class MarkerMovement(
    val markerId: MapMarkerId,
    val position: GeoPoint
)

data class Rgb(val r: Int, val g: Int, val b: Int) {
    fun css(): String = "${r.coerceIn(0, 255)}, ${g.coerceIn(0, 255)}, $${b.coerceIn(0, 255)}"
}

typealias OnFocus = (() -> Unit) -> Unit