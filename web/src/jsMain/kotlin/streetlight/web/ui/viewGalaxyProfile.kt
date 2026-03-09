package streetlight.web.ui

import koala.dom.*
import koala.html.TabClass
import streetlight.model.data.Galaxy
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.model.AppContext
import streetlight.web.shells.GalaxyShell
import streetlight.web.shells.HomeShell
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

fun RenderContext.viewGalaxyProfileRoute(app: AppContext) {
    routeBlock<GalaxyPathIdRoute, Galaxy>(app.portal, { route ->
        app.client.api.readGalaxy(route.pathId)
    }) { galaxy ->
        shellBox(GalaxyShell.galaxyBoxId, app.geoMap, app.appScope) {
            galaxyShell(galaxy, emptyList())
        }
    }
}