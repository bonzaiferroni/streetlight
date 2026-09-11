package streetlight.web.ui

import kampfire.model.toDataOr
import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.model.data.EntityFeed
import streetlight.model.ui.HomeRoute
import streetlight.web.model.MarkerService
import streetlight.web.model.MarkerMap

fun ViewScope.wireStreetMap(feed: EntityFeed) {
    val markerMap = app.get<MarkerMap>()
    val markerService = app.get<MarkerService>()
    val points = markerService.createMarkers(feed.entities)
    markerMap.setPoints(points)
}