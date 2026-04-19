package koala.model

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.Url
import kampfire.model.distanceTo
import kampfire.model.meters
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

    private val _entityFlow = MutableSharedFlow<List<MapEntity>>(1)
    val entityFlow: SharedFlow<List<MapEntity>> = _entityFlow
    private val _removeEntity = MutableSharedFlow<List<MapEntityId>>(1)
    val removeEntity: SharedFlow<List<MapEntityId>> = _removeEntity
    private val _linesFlow = MutableSharedFlow<List<LineEntity>>(1)
    val linesFlow: SharedFlow<List<LineEntity>> = _linesFlow
    private val _panFlow = MutableSharedFlow<PanPoint>(1)
    val panFlow: SharedFlow<PanPoint> = _panFlow
    private val _markerVisibilityFlow = MutableSharedFlow<(MapEntity) -> Boolean>(1)
    val markerVisibilityFlow: SharedFlow<(MapEntity) -> Boolean> = _markerVisibilityFlow
    private val _movementFlow = MutableSharedFlow<EntityMovement>(1)
    val movementFlow: Flow<EntityMovement> = _movementFlow
    private val _tempEntityFlow = MutableSharedFlow<TempEntitySet?>(1)
    val tempEntityFlow: Flow<TempEntitySet?> = _tempEntityFlow
    private val _hideLayersFlow = MutableSharedFlow<List<LayerId>>()
    val hideLayersFlow: Flow<List<LayerId>> = _hideLayersFlow
    private val _showLayersFlow = MutableSharedFlow<List<LayerId>>()
    val showLayersFlow: Flow<List<LayerId>> = _showLayersFlow

    val viewedStateFlow = stateFlow.filter { it.isViewed }
    val zoomFlow = viewedStateFlow.mapDistinct { it.zoom }
    val movingBoundsFlow = viewedStateFlow.mapDistinct { it.movingBounds }
    val centerFlow = viewedStateFlow.filter { !it.isMoving }.mapDistinct { it.center }
    val boundsFlow = viewedStateFlow.filter { it.isViewed }.mapDistinct { it.bounds }

    fun addEntity(entity: MapEntity) {
        addEntities(listOf(entity))
    }

    fun addEntities(entities: List<MapEntity>) {
        scope.launch {
            _entityFlow.emit(entities)
        }
    }

    fun moveEntity(entityId: MapEntityId, position: GeoPoint) {
        scope.launch {
            _movementFlow.emit(EntityMovement(entityId, position))
        }
    }

    fun removeEntities(entityIds: List<MapEntityId>) {
        scope.launch {
            _removeEntity.emit(entityIds)
        }
    }

    fun addLines(entities: List<LineEntity>) {
        scope.launch {
            _linesFlow.emit(entities)
        }
    }

    fun setEntityVisibility(filter: (MapEntity) -> Boolean) {
        scope.launch {
            _markerVisibilityFlow.emit(filter)
        }
    }

    fun setFocus(entity: PointEntity?) {
        state.set { it.copy(focus = entity) }
    }

    fun setBounds(center: GeoPoint, value: GeoBounds, zoom: Float, isMoving: Boolean) {
        val isSmallMovement = isMoving && zoom == stateNow.zoom && value.center.distanceTo(stateNow.center) < (20 * zoom).meters
        if (!isSmallMovement) {
            val bounds = if (isMoving) stateNow.bounds else value
            state.set { it.copy(center = center, bounds = bounds, movingBounds = value, zoom = zoom, isMoving = isMoving) }
        }
    }

    fun panMap(point: GeoPoint) {
        panMap(PanPoint(point))
    }

    fun panMap(pan: PanPoint) {
        scope.launch {
            _panFlow.emit(pan)
        }
    }

    fun setIsViewed(value: Boolean) {
        state.set { it.copy(isViewed = value) }
    }

    fun tempEntities(entities: List<MapEntity>?) {
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
    val movingBounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val isMoving: Boolean = false,
    val isViewed: Boolean = false,
    val focus: PointEntity? = null,
)

typealias MapEntityId = String
typealias MapContextId = String

sealed interface MapEntity {
    val entityId: MapEntityId
    val label: String? get() = null
}

data class PanPoint(
    val point: GeoPoint,
    val zoom: Float? = null,
    val snap: Boolean = false,
)

interface PointEntity: MapEntity {
    val position: GeoPoint
    val bearing: Float? get() = null
    val opacity: Float? get() = null
    val subpixelPositioning: Boolean get() = true
    val icon: Svg? get() = null
    val thumbUrl: Url? get() = null
    val minZoom: Float? get() = null
    val modifiers: ModifierSet? get() = null
    val onFocus: OnFocus? get() = null
    val focusCard: (RenderContext.() -> Unit)? get() = null
    val body: (DIV.() -> Unit)? get() = null
    val light: Rgb? get() = null
}

data class EntityMovement(
    val entityId: MapEntityId,
    val position: GeoPoint
)

data class Rgb(val r: Int, val g: Int, val b: Int) {
    fun css(): String = "${r.coerceIn(0, 255)}, ${g.coerceIn(0, 255)}, $${b.coerceIn(0, 255)}"
}

typealias OnFocus = (() -> Unit) -> Unit