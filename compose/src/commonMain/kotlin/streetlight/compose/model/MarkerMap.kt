package streetlight.compose.model

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

    /** Focuses [marker] and pans to it, or clears the focus when it is `null`. */
    fun setFocus(marker: MapMarker?) {
        store.set(store.now.copy(focus = marker))
        marker?.let { geoMap.camera.pan(it.geoPoint) }
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
