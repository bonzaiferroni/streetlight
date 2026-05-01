package streetlight.web.ui

import koala.dom.*
import streetlight.web.GalaxySlugRoute
import streetlight.web.io.getDataOrNull
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyKey
import streetlight.web.shells.GalaxyContent
import streetlight.web.shells.galaxyShell

fun ViewContext<Streetlight>.viewGalaxy(content: GalaxyContent) {
    val app = model

    val root = shellBox(GalaxyKey.ShellId) {
        galaxyShell(content)
    }

    // queryAndWireSwitch(root, GalaxyProfileKey.MapSwitchId, onToggle = ::setIsMapVisible, bindFlow = isMapVisibleFlow)
    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = app.cache.eventLights
    )
    wireGalaxyMenu(app, root, content.galaxy)

    app.streetMap.setPosts(content.posts)
}

fun ViewContext<Streetlight>.viewGalaxyRoute() {
    routeBlock<GalaxySlugRoute, GalaxyContent>(model.portal, { route ->
        readIslandOrApi(GalaxyKey.GalaxyContentId) {
            val galaxy = api.readGalaxy(route.slug) ?: return@routeBlock null
            val listing = api.readPosts(galaxy.galaxyId).getDataOrNull() ?: return@routeBlock null
            GalaxyContent(
                galaxy = galaxy,
                posts = listing,
            )
        }
    }) { content ->
        viewContextOf(model) {
            viewGalaxy(content)
        }
    }
}