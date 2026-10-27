package streetlight.compose.model

import kampfire.model.GeoPoint
import kampfire.model.GeoRect
import kampfire.model.Tap
import kampfire.model.storeOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** The camera over the app's one map: the requests to move it, and where it looks as the map reports it. */
class GeoCamera {
    private val store = storeOf(GeoCameraState())
    private val pans = MutableSharedFlow<PanPoint>(extraBufferCapacity = 1)

    val cameraState: Tap<GeoCameraState> = store

    /** The requests to pan, applied by the map as they come. */
    val panFlow: SharedFlow<PanPoint> = pans

    /** Pans the map to [point], at [zoom] when given. */
    fun pan(point: GeoPoint, zoom: Double? = null) {
        pans.tryEmit(PanPoint(point, zoom))
    }

    /** Sets where the camera looks, as the map reports it. */
    fun report(state: GeoCameraState) = store.set(state)
}

/** Where the camera looks: its [center], the [bounds] in view, its [zoom], and whether it [isMoving]. */
data class GeoCameraState(
    val center: GeoPoint? = null,
    val bounds: GeoRect? = null,
    val zoom: Double? = null,
    val isMoving: Boolean = false,
)

/** A point to pan to, at [zoom] when given. */
data class PanPoint(val point: GeoPoint, val zoom: Double? = null)
