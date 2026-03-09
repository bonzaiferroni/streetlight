package streetlight.web.ui

import koala.dom.*
import streetlight.model.data.Galaxy
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.model.AppContext

fun RenderContext.viewGalaxyProfile(galaxy: Galaxy) {
    column {
        textBlock("yer galaxy: ${galaxy.name}")
    }
}

fun RenderContext.viewGalaxyProfileRoute(app: AppContext) {
    routeBlock<GalaxyPathIdRoute, Galaxy>(app.portal, { route ->
        app.client.api.readGalaxy(route.pathId)
    }) { galaxy ->
        viewGalaxyProfile(galaxy)
    }
}