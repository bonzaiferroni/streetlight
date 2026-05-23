package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.GalaxyContent
import streetlight.web.GalaxyRoute
import streetlight.web.model.DataCache
import streetlight.web.shells.GalaxyKey
import streetlight.web.shells.galaxyShell

fun RenderContext.viewGalaxy(content: GalaxyContent) {
    val cache = app.get<DataCache>()

    val root = shellBox(GalaxyKey.ShellId) {
        galaxyShell(content)
    }

    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = cache.eventLights
    )
    wireGalaxyMenu(root, content.galaxy)

    streetMap.setPosts(content.posts)
}

fun RenderContext.viewGalaxyRoute() {
    routeBlock<GalaxyRoute, GalaxyContent>(portal, { route ->
        readIsland<GalaxyContent>(GalaxyKey.GalaxyContentId) { it.galaxy.galaxyId.toString() == route.value || it.galaxy.slug == route.value }
            ?: api.readGalaxyContent(route.value).handleResponse(toaster::toast)
    }) { content ->
        viewGalaxy(content)
    }
}