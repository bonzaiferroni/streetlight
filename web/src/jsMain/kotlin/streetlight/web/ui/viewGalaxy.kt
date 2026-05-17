package streetlight.web.ui

import kampfire.model.getDataOrNull
import kampfire.model.handleResponse
import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.web.GalaxyRoute
import streetlight.web.layouts.PostKey
import streetlight.web.layouts.layoutPosts
import streetlight.web.model.DataCache
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyKey
import streetlight.web.shells.GalaxyContent
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
        readIslandOrApi(GalaxyKey.GalaxyContentId, { it.galaxy.galaxyId.toString() == route.id || it.galaxy.slug == route.id}) {
            val galaxy = api.readGalaxy(route.id).handleResponse(toaster::toast) ?: return@routeBlock null
            val listing = api.readPosts(galaxy.galaxyId).handleResponse(toaster::toast) ?: return@routeBlock null
            GalaxyContent(
                galaxy = galaxy,
                posts = listing,
            )
        }
    }) { content ->
        viewGalaxy(content)
    }
}