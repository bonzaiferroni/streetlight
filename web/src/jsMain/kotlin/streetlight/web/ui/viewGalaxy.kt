package streetlight.web.ui

import streetlight.model.ui.toRoute
import koala.dom.*
import streetlight.model.data.GalaxyContent
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.toConfigRoute
import streetlight.web.model.RouteDockState
import streetlight.web.shells.GalaxyShell
import streetlight.web.shells.galaxyShell

fun ViewScope.viewGalaxy(content: GalaxyContent) {
    shellBox {
        galaxyShell(content)
        applyTheme(content.design?.theme)
    }

    val rightRoutes = content.galaxy.takeIf { it.isHost }?.toConfigRoute()?.let { listOf(it) }
    dock.mergeState(content.galaxy.toRoute(), RouteDockState(title = content.galaxy.name, rightRoutes = rightRoutes))

    // wireGalaxyMenu(root, content.galaxy)

    markerMap.setPoints(content.feed.entities)
}

fun RouteScope.viewGalaxyRoute() {
    routeBlock<GalaxyRoute, GalaxyContent>(GalaxyShell.islandId) { content ->
        viewGalaxy(content)
    }
}

// val hookInitializers: List<ViewScope.(HTMLElement) -> Unit> = listOf(
//     ViewScope::initPostMenu
// )
