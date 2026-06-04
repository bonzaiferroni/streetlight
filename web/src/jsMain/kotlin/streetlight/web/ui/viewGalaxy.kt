package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.GalaxyContent
import streetlight.web.GalaxyRoute
import streetlight.web.model.DataCache
import streetlight.web.model.MapEntityService
import streetlight.web.shells.GalaxyKey
import streetlight.web.shells.galaxyShell

fun RenderContext.viewGalaxy(content: GalaxyContent) {
    val cache = app.get<DataCache>()
    val mapEntityService = app.get<MapEntityService>()

    val root = shellBox(GalaxyKey.ShellId) {
        galaxyShell(content)
    }

    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = cache.eventLights
    )
    wireGalaxyMenu(root, content.galaxy)

    val points = mapEntityService.createEntities(content.posts)
    pointMap.setPoints(points)
}

fun RenderContext.viewGalaxyRoute() {
    routeBlock<GalaxyRoute, GalaxyContent>(portal, { route ->
        readIsland<GalaxyContent>(GalaxyKey.GalaxyContentId) { it.galaxy.slug == route.slug }
            ?: api.readGalaxyContent(route.slug).handleResponse(toaster::toast)
    }) { content ->
        viewGalaxy(content)
    }
}