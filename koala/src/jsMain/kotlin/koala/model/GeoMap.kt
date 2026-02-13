package koala.model

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.meters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class GeoMap(
    scope: CoroutineScope
): BrowserModel<GeoMapState>(GeoMapState(), scope) {
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
    val centerFlow = viewedStateFlow.mapDistinct { it.center }
    val boundsFlow = viewedStateFlow.filter { it.isViewed }.mapDistinct { it.bounds }

    fun addEntities(entities: List<MapEntity>) {
        viewModelScope.launch {
            _entityFlow.emit(entities)
        }
    }

    fun removeEntities(entityIds: List<MapEntityId>) {
        viewModelScope.launch {
            _removeEntity.emit(entityIds)
        }
    }

    fun addLines(entities: List<LineEntity>) {
        viewModelScope.launch {
            _linesFlow.emit(entities)
        }
    }

    fun setEntityVisibility(filter: (MapEntity) -> Boolean) {
        viewModelScope.launch {
            _markerVisibilityFlow.emit(filter)
        }
    }

    fun setBounds(value: GeoBounds, zoom: Float, isMoving: Boolean) {
        if (isMoving && zoom == stateNow.zoom && value.center.distanceTo(stateNow.center) < (20 * zoom).meters) return
        val bounds = if (isMoving) stateNow.bounds else value
        setState { it.copy(bounds = bounds, movingBounds = value, zoom = zoom, isMoving = isMoving) }
    }

    fun panMap(pan: PanPoint) {
        viewModelScope.launch {
            _panFlow.emit(pan)
        }
    }

    fun setIsViewed(value: Boolean) {
        setState { it.copy(isViewed = value) }
    }
}

data class GeoMapState(
    val bounds: GeoBounds = GeoBounds.Denver,
    val movingBounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val isMoving: Boolean = false,
    val isViewed: Boolean = false,
) {
    val center get() = bounds.center
}

typealias MapEntityId = String

sealed interface MapEntity {
    val entityId: MapEntityId
}

data class PanPoint(
    val point: GeoPoint,
    val zoom: Float? = null,
    val snap: Boolean = false,
)