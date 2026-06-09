package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.GalaxyContent
import streetlight.web.GalaxyRoute
import streetlight.web.model.DataCache
import streetlight.web.model.MarkerService
import streetlight.web.shells.GalaxyKey
import streetlight.web.shells.galaxyShell

fun RenderContext.viewGalaxy(content: GalaxyContent) {
    val cache = app.get<DataCache>()
    val markerService = app.get<MarkerService>()

    val root = shellBox(GalaxyKey.ShellId) {
        galaxyShell(content)
    }

    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = cache.eventLights
    )
    wireGalaxyMenu(root, content.galaxy)

    val points = markerService.createMarkers(content.posts)
    markerMap.setPoints(points)
}

fun RenderContext.viewGalaxyRoute() {
    routeBlock<GalaxyRoute, GalaxyContent>(portal, { route ->
        readIsland<GalaxyContent>(GalaxyKey.GalaxyContentId) { it.galaxy.slug == route.slug }
            ?: api.readGalaxyContent(route.slug).handleResponse(toaster::toast)
    }) { content ->
        viewGalaxy(content)
    }
}