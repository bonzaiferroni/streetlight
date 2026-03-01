package koala.model

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.distanceTo
import kampfire.model.meters
import koala.dom.RenderContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class GeoMap(
    private val scope: CoroutineScope
) {
    private val state = storeOf(GeoMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    private val _entityFlow = MutableSharedFlow<List<MapEntity>>(8)
    val entityFlow: SharedFlow<List<MapEntity>> = _entityFlow
    private val _removeEntity = MutableSharedFlow<List<MapEntityId>>(8)
    val removeEntity: SharedFlow<List<MapEntityId>> = _removeEntity
    private val _linesFlow = MutableSharedFlow<List<LineEntity>>(8)
    val linesFlow: SharedFlow<List<LineEntity>> = _linesFlow
    private val _panFlow = MutableSharedFlow<PanPoint>(8)
    val panFlow: SharedFlow<PanPoint> = _panFlow
    private val _markerVisibilityFlow = MutableSharedFlow<(MapEntity) -> Boolean>(8)
    val markerVisibilityFlow: SharedFlow<(MapEntity) -> Boolean> = _markerVisibilityFlow

    val viewedStateFlow = stateFlow.filter { it.isViewed }
    val zoomFlow = viewedStateFlow.mapDistinct { it.zoom }
    val movingBoundsFlow = viewedStateFlow.mapDistinct { it.movingBounds }
    val centerFlow = viewedStateFlow.filter { !it.isMoving }.mapDistinct { it.center }
    val boundsFlow = viewedStateFlow.filter { it.isViewed }.mapDistinct { it.bounds }

    fun addEntities(entities: List<MapEntity>) {
        scope.launch {
            _entityFlow.emit(entities)
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

    fun setBounds(value: GeoBounds, zoom: Float, isMoving: Boolean, nearest: PointEntity?) {
        if (isMoving && zoom == stateNow.zoom && value.center.distanceTo(stateNow.center) < (20 * zoom).meters) return
        val bounds = if (isMoving) stateNow.bounds else value
        state.set { it.copy(bounds = bounds, movingBounds = value, zoom = zoom, isMoving = isMoving, nearest = nearest) }
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
}

data class GeoMapState(
    val bounds: GeoBounds = GeoBounds.Denver,
    val movingBounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val isMoving: Boolean = false,
    val isViewed: Boolean = false,
    val nearest: PointEntity? = null,
) {
    val center get() = bounds.center
}

typealias MapEntityId = String

sealed interface MapEntity {
    val entityId: MapEntityId
    val label: String
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
    val iconPath: String? get() = null
    val thumbPath: String? get() = null
    val minZoom: Float? get() = null
    val onClick: (() -> Unit)? get() = null
    val focusCard: (RenderContext.() -> Unit)? get() = null
}