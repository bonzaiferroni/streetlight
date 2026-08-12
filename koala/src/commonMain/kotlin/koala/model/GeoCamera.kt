package koala.model

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class GeoCamera(
    private val scope: CoroutineScope,
) {
    private val state = storeOf(GeoCameraState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    private val _panFlow = MutableSharedFlow<PanPoint>(1)
    internal val panFlow: SharedFlow<PanPoint> = _panFlow
    private val _panBoundsFlow = MutableSharedFlow<GeoBounds>(1)
    internal val panBoundsFlow: SharedFlow<GeoBounds> = _panBoundsFlow
    private val _movementFlow = MutableSharedFlow<MarkerMovement>(1)
    internal val movementFlow: Flow<MarkerMovement> = _movementFlow

    // val viewedStateFlow = stateFlow.filter { it.isViewed }
    // val isMovingFlow = viewedStateFlow.dedup { it.isMoving }
    // val settledStateFlow = viewedStateFlow.filter { !it.isMoving }
    // val zoomFlow = settledStateFlow.dedup { it.zoom }
    // val centerFlow = settledStateFlow.dedup { it.center }
    // val boundsFlow = settledStateFlow.dedup { it.bounds }
    // val movingBoundsFlow = viewedStateFlow.dedup { it.bounds }
    // val focusFlow = viewedStateFlow.dedup { it.focus }

    val pointAndZoom = state.tapOf { it.center to it.zoom }

    val viewedState = state.refine { states -> states.filter { it.isViewed } }
    val settledState = viewedState.refine { states -> states.filter { !it.isMoving } }

    val isMovingField = viewedState.tapOf { it.isMoving }
    val movingBoundsField = viewedState.tapOf { it.bounds }
    val focusField = viewedState.tapOf { it.focus }
    val zoomField = settledState.tapOf { it.zoom }
    val centerField = settledState.tapOf { it.center }
    val boundsField = settledState.tapOf { it.bounds }

    fun panMap(point: GeoPoint) {
        panMap(PanPoint(point))
    }

    fun panMap(bounds: GeoBounds) {
        scope.launch {
            _panBoundsFlow.emit(bounds)
        }
    }

    fun panMap(pan: PanPoint) {
        if (pan.point.isTouching(stateNow.center) && (pan.zoom == null || pan.zoom == stateNow.zoom)) return
        scope.launch {
            _panFlow.emit(pan)
        }
    }

    internal fun setIsViewed(value: Boolean) {
        state.set { copy(isViewed = value) }
    }

    internal fun setBounds(center: GeoPoint, bounds: GeoBounds, zoom: Float, isMoving: Boolean) {
        state.set { copy(center = center, bounds = bounds, zoom = zoom, isMoving = isMoving) }
    }
}

data class GeoCameraState(
    val center: GeoPoint = GeoPoint.Denver,
    val bounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val isMoving: Boolean = false,
    val isViewed: Boolean = false,
    val focus: PointMarker? = null,
)

data class PanPoint(
    val point: GeoPoint,
    val zoom: Float? = null,
    val snap: Boolean = false,
)