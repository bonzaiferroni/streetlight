package streetlight.web

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.meters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class GeoMap(
    scope: CoroutineScope
): BrowserModel<GeoMapState>(GeoMapState(), scope) {

    private val _entityFlow = MutableSharedFlow<MapEntity>()
    val entityFlow: SharedFlow<MapEntity> = _entityFlow
    private val _removeEntity = MutableSharedFlow<MapEntityId>()
    val removeEntity: SharedFlow<MapEntityId> = _removeEntity

    val zoomFlow = stateFlow.mapDistinct { it.zoom }

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

    fun setBounds(bounds: GeoBounds, zoom: Float) {
        if (zoom == stateNow.zoom && bounds.center.distanceTo(stateNow.center) < (10 * zoom).meters) return
        setState { it.copy(bounds = bounds, zoom = zoom) }
    }
}

data class GeoMapState(
    val bounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
) {
    val center get() = bounds.center
}

typealias MapEntityId = String

sealed interface MapEntity {
    val entityId: MapEntityId
}

interface MarkerEntity: MapEntity {
    val position: GeoPoint
    val bearing: Float? get() = null
    val opacity: Float? get() = null
    val subpixelPositioning: Boolean get() = true
    val iconPath: String? get() = null
    val minZoom: Float? get() = null
    val onClick: (() -> Unit)? get() = null
}