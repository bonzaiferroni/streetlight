package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.web.HomeRoute
import streetlight.web.model.DataCache
import streetlight.web.model.MarkerService
import streetlight.web.model.MarkerMap

fun RenderContext.wireStreetMap() {
    val markerMap = app.get<MarkerMap>()
    val cache = app.get<DataCache>()
    val markerService = app.get<MarkerService>()

    renderScope.launch {
        portal.routeFlowOf<HomeRoute>().collect {
            val galaxyIds = cache.topGalaxies.getItems().map { it.galaxyId }
            // td: gather initial posts from json in html
            val posts = api.readPosts(galaxyIds).handleResponse(toaster::toast) ?: return@collect
            val points = markerService.createMarkers(posts)
            markerMap.setPoints(points)
        }
    }
}