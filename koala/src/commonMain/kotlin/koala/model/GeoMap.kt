package koala.model

import kampfire.model.Tap
import kampfire.model.storeOf

/** The app's one map: the [camera] over it, and the [MarkerMap] of the view it shows, if any. */
class GeoMap(val camera: GeoCamera) {
    private val store = storeOf<MarkerMap?>(null)

    val markerMapState: Tap<MarkerMap?> = store

    /** Shows the layers of [markerMap], or none when it is `null`. */
    fun show(markerMap: MarkerMap?) = store.set(markerMap)
}
