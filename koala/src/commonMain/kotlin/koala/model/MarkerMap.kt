package koala.model

import kampfire.model.Tap
import kampfire.model.getContainingBounds
import kampfire.model.storeOf
import kampfire.model.tapOf

/**
 * The layers of markers a view shows on the [geoMap] and the one in focus. A model holds one for the map its view
 * shows, and the map shows its layers while that view's mount is the one in sight.
 */
class MarkerMap(val geoMap: GeoMap) {
    private val state = storeOf(MarkerMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val focusState = state.tapOf { it.focus }

    private val layersStore = storeOf<List<GeoLayer>>(emptyList())
    val layersState: Tap<List<GeoLayer>> = layersStore
    val markerLayer = getOrCreateLayer(MarkerLayerConfig.Markers)

    val centerNow get() = geoMap.camera.stateNow.center
    val isMovingState = geoMap.camera.isMovingState

    /** The layer with the id in [config], created when there is none. */
    fun getOrCreateLayer(config: GeoLayerConfig) = layersStore.now.firstOrNull { it.layerId == config.layerId }
        ?: GeoLayer(config).also { layer -> layersStore.set(layersStore.now + layer) }

    /** Replaces the markers of the [markerLayer] with [markers], keeping the focus only while it is among them. */
    fun setMarkers(markers: List<StaticMarker>) {
        markerLayer.setPoints(markers)
        state.set { copy(markers = markers, focus = focus?.takeIf { f -> markers.any { it.markerId == f.markerId } }) }
    }

    /** Keeps the markers that match [predicate]. */
    fun filterPoints(predicate: (StaticMarker) -> Boolean) = setMarkers(stateNow.markers.filter(predicate))

    /** Focuses [marker], leaving the camera where it is, or clears the focus when it is `null`. */
    fun setFocus(marker: PointMarker?) = state.set { copy(focus = marker) }

    /** Frames every marker, or pans to the point of the only one. */
    fun showAll() {
        val markers = stateNow.markers.takeIf { it.isNotEmpty() } ?: return
        when (val bounds = getContainingBounds(markers.map { it.geoPoint })) {
            null -> geoMap.camera.panMap(markers.first().geoPoint)
            else -> geoMap.camera.panMap(bounds.scaleBy(AreaScale))
        }
    }
}

/** The markers of a [MarkerMap] and the one in focus. */
data class MarkerMapState(
    val markers: List<StaticMarker> = emptyList(),
    val focus: PointMarker? = null,
)

/** The scale of the markers' bounds in the area that frames them. */
private const val AreaScale = 1.5f
