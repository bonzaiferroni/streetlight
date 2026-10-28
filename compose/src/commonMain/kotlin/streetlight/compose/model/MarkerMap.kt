package streetlight.compose.model

import kampfire.model.GeoRect
import kampfire.model.getContainingBounds
import kampfire.model.storeOf
import kampfire.model.tapOf
import streetlight.model.data.Entity

/**
 * The markers a view shows on the [geoMap] and the one in focus. A view model holds one for the map it shows, and the
 * map shows its markers while the view's map is the one in sight.
 */
class MarkerMap(private val geoMap: GeoMap) {
    private val store = storeOf(MarkerMapState())

    val markersState = store.tapOf { it.markers }
    val focusState = store.tapOf { it.focus }

    /** Replaces the markers with those of [entities]. */
    fun setPoints(entities: List<Entity>) = setMarkers(entities.toMarkers(emptyList()))

    /** Adds the markers of the [entities] not yet shown. */
    fun addPoints(entities: List<Entity>) = setMarkers(entities.toMarkers(store.now.markers))

    /** The area that frames every marker, the bounds of their points scaled by half again, `null` for fewer than two. */
    val area: GeoRect?
        get() = getContainingBounds(store.now.markers.map { it.geoPoint })?.scaleBy(AreaScale)

    /** Pans the map to frame every marker, or to the point of the only one. */
    fun showAll() {
        val markers = store.now.markers.takeIf { it.isNotEmpty() } ?: return
        when (val area = area) {
            null -> geoMap.camera.pan(markers.first().geoPoint)
            else -> geoMap.camera.pan(area)
        }
    }

    /** Focuses [marker], leaving the camera where it is, or clears the focus when it is `null`. */
    fun setFocus(marker: MapMarker?) {
        store.set(store.now.copy(focus = marker))
    }

    private fun setMarkers(markers: List<MapMarker>) {
        val focus = store.now.focus?.takeIf { focus -> markers.any { it.markerId == focus.markerId } }
        store.set(MarkerMapState(markers, focus))
    }
}

/** The markers of a [MarkerMap] and the one in focus. */
data class MarkerMapState(
    val markers: List<MapMarker> = emptyList(),
    val focus: MapMarker? = null,
)

/** The [markers], followed by those of these entities that it does not hold. */
private fun List<Entity>.toMarkers(markers: List<MapMarker>): List<MapMarker> {
    val ids = markers.mapTo(mutableSetOf()) { it.markerId }
    return markers + mapNotNull { entity -> entity.toMarker()?.takeIf { ids.add(it.markerId) } }
}

/** The scale of the markers' bounds in the area that frames them, as the web's `showAll`. */
private const val AreaScale = 1.5f
