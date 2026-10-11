package streetlight.web.view

import kampfire.model.toDataOrNull
import koala.dom.*
import koala.dom.page.*
import streetlight.model.data.GalaxyFeedContent
import streetlight.model.ui.GalaxyFeedRoute
import streetlight.web.dom.page.applyTheme
import streetlight.web.model.api
import streetlight.web.model.toaster
import streetlight.web.shell.GalaxyFeedShell
import streetlight.web.shell.galaxyFeedShell
import streetlight.web.utility.setTitle
import web.dom.document

fun ViewScope.viewGalaxyFeed(content: GalaxyFeedContent) {
    shellBox {
        galaxyFeedShell(content)
    }

    document.setTitle(GalaxyFeedRoute())
    applyTheme(null)

    followFeedRoute<GalaxyFeedRoute>({ GalaxyFeedRoute(it) }) { route ->
        api.content.readGalaxyFeedContent(route.feed).toDataOrNull(toaster) { it.feed }
    }
}

fun RouteScope.viewGalaxyFeedRoute() {
    routeBlock<GalaxyFeedRoute, GalaxyFeedContent>(GalaxyFeedShell.IslandId) { content ->
        viewGalaxyFeed(content)
    }
}
