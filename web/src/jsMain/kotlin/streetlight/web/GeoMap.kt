package streetlight.web

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.meters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class GeoMap(
    private val viewModelScope: CoroutineScope
) {
    private var state: GeoMapState = GeoMapState()
    val stateNow get() = state

    private val _state = MutableSharedFlow<GeoMapState>()
    val stateFlow: Flow<GeoMapState> = _state
    private val _entityFlow = MutableSharedFlow<MapEntity>()
    val entityFlow: SharedFlow<MapEntity> = _entityFlow
    private val _removeEntity = MutableSharedFlow<MapEntityId>()
    val removeEntity: SharedFlow<MapEntityId> = _removeEntity
    private val _linesFlow = MutableSharedFlow<List<LineEntity>>()
    val linesFlow: SharedFlow<List<LineEntity>> = _linesFlow
    private val _panFlow = MutableSharedFlow<PanPoint>()
    val panFlow: SharedFlow<PanPoint> = _panFlow

    val zoomFlow = stateFlow.mapDistinct { it.zoom }
    val movingBoundsFlow = stateFlow.mapDistinct { it.movingBounds }
    val centerFlow = stateFlow.mapDistinct { it.center }
    val boundsFlow = stateFlow.mapDistinct { it.bounds }

    private fun setState(setter: (GeoMapState) -> GeoMapState) {
        state = setter(stateNow)
        viewModelScope.launch {
            _state.emit(state)
        }
    }

    fun addEntity(entity: MapEntity) {
        viewModelScope.launch {
            _entityFlow.emit(entity)
        }
    }

    fun addEntities(entities: List<MapEntity>) {
        viewModelScope.launch {
            entities.forEach {
                _entityFlow.emit(it)
            }
        }
    }

    fun removeEntities(entityIds: List<MapEntityId>) {
        viewModelScope.launch {
            entityIds.forEach {
                _removeEntity.emit(it)
            }
        }
    }

    fun addLines(entities: List<LineEntity>) {
        viewModelScope.launch {
            _linesFlow.emit(entities)
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
        console.log("ey: $value")
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