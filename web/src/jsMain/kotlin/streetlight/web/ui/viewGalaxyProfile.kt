package streetlight.web.ui

import koala.dom.*
import streetlight.model.data.Galaxy
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyShell
import streetlight.web.shells.GalaxyShellContent
import streetlight.web.shells.galaxyShell

fun RenderContext.viewGalaxyProfile(galaxy: Galaxy) {
//    column {
//        tabs(HomeShell.tabsId) {
//            tab("Spotlight") {
//                headerOf(galaxy)
//            }
//            tab("Map") {
//
//            }
//            tab("Talk") {
//
//            }
//        }
//    }
}

fun RenderContext.viewGalaxyProfileRoute(app: Streetlight) {
    routeBlock<GalaxyPathIdRoute, GalaxyShellContent>(app.portal, { route ->
        val galaxy = app.client.api.readGalaxy(route.pathId) ?: return@routeBlock null
        val posts = app.client.api.readPosts(galaxy.galaxyId) ?: return@routeBlock null
        GalaxyShellContent(
            galaxy = galaxy,
            posts = posts
        )
    }) { content ->
        shellBox(GalaxyShell.galaxyBoxId, app.geoMap, app.appScope) {
            galaxyShell(content)
        }
    }
}