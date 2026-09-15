package koala.model

import kampfire.model.GeoRect
import kampfire.model.GeoPoint
import kampfire.model.refine
import kampfire.model.storeOf
import kampfire.model.tapOf
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

    internal val panFlow: SharedFlow<PanPoint> field = MutableSharedFlow<PanPoint>(1)
    internal val panBoundsFlow: SharedFlow<GeoRect> field = MutableSharedFlow<GeoRect>(1)
    internal val movementFlow: Flow<MarkerMovement> field = MutableSharedFlow<MarkerMovement>(1)

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
    val settledView = settledState.tapOf { it.bounds }

    fun panMap(point: GeoPoint) {
        panMap(PanPoint(point))
    }

    fun panMap(bounds: GeoRect) {
        scope.launch {
            panBoundsFlow.emit(bounds)
        }
    }

    fun panMap(pan: PanPoint) {
        if (pan.point.isTouching(stateNow.center) && (pan.zoom == null || pan.zoom == stateNow.zoom)) return
        scope.launch {
            panFlow.emit(pan)
        }
    }

    internal fun setIsViewed(value: Boolean) {
        state.set { copy(isViewed = value) }
    }

    internal fun setBounds(center: GeoPoint, bounds: GeoRect, zoom: Float, isMoving: Boolean) {
        state.set { copy(center = center, bounds = bounds, zoom = zoom, isMoving = isMoving) }
    }
}

data class GeoCameraState(
    val center: GeoPoint = GeoPoint.Denver,
    val bounds: GeoRect = GeoRect.Denver,
    val zoom: Float = 11f,
    val isMoving: Boolean = true,
    val isViewed: Boolean = false,
    val focus: PointMarker? = null,
)

data class PanPoint(
    val point: GeoPoint,
    val zoom: Float? = null,
    val snap: Boolean = false,
)