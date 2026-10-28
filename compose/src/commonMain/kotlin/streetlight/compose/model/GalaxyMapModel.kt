package streetlight.compose.model

import androidx.lifecycle.ViewModel
import kampfire.model.tapOf
import streetlight.model.data.GalaxyContent

/** The map of a galaxy's [content]: the [markerMap] of its feed's first page, and the entity of the marker in focus. */
class GalaxyMapModel(val content: GalaxyContent, geoMap: GeoMap) : ViewModel() {
    val markerMap = MarkerMap(geoMap)

    val focusEntitiesState = markerMap.focusState.tapOf { focus -> focus?.entity?.let { listOf(it) } }

    init {
        markerMap.setPoints(content.feed.entities)
    }

    /** The area the map frames on arrival, `null` to look over [center]. */
    val area = markerMap.area

    /** The point the map looks over without an [area]: the only marker's, or the galaxy's. */
    val center = markerMap.markersState.now.singleOrNull()?.geoPoint ?: content.galaxy.geoPoint
}
