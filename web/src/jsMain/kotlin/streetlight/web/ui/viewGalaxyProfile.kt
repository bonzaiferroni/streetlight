package streetlight.web.ui

import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyShell
import streetlight.web.shells.GalaxyShellContent
import streetlight.web.shells.galaxyShell

fun RenderContext.viewGalaxyProfile(app: Streetlight, content: GalaxyShellContent) {
    shellBox(GalaxyShell.galaxyBoxId, app.geoMap, app.appScope) {
        galaxyShell(content)
    }

    app.streetMap.setPosts(content.posts)
}

fun ViewContext<Streetlight>.viewGalaxyProfileRoute() {
    routeBlock<GalaxyPathIdRoute, GalaxyShellContent>(model.portal, { route ->
        val galaxy = model.client.api.readGalaxy(route.pathId) ?: return@routeBlock null
        val posts = model.client.api.readPosts(galaxy.galaxyId) ?: return@routeBlock null
        GalaxyShellContent(
            galaxy = galaxy,
            posts = posts
        )
    }) { content ->
        viewGalaxyProfile(model, content)
    }
}