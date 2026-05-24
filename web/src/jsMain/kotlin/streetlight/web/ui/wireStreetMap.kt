package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.Class
import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.web.HomeRoute
import streetlight.web.model.DataCache
import streetlight.web.model.StreetMap

fun RenderContext.wireStreetMap() {
    val streetMap = app.get<StreetMap>()
    val cache = app.get<DataCache>()

    renderScope.launch {
        portal.routeFlowOf<HomeRoute>().collect {
            val galaxyIds = cache.topGalaxies.getItems().map { it.galaxyId }
            // td: gather initial posts from json in html
            val posts = api.readPosts(galaxyIds).handleResponse(toaster::toast) ?: return@collect
            streetMap.setPosts(posts)
        }
    }
}

object MapPanel {
    val container = Class("map-event-panel")
    val card = Class("map-panel-card")
    val cardHeading = Class("map-panel-card-heading")
    val grid = Class("map-panel-card-grid")
}