package streetlight.web.view

import streetlight.model.ui.toRoute
import koala.dom.*
import streetlight.model.data.GalaxyContent
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.toConfigRoute
import streetlight.web.model.RouteDockState
import streetlight.web.shell.GalaxyShell
import streetlight.web.shell.galaxyShell
import koala.dom.page.*
import streetlight.web.dom.page.applyTheme
import streetlight.web.model.dock
import streetlight.web.interop.wireGalaxyMenu

fun ViewScope.viewGalaxy(content: GalaxyContent) {
    shellBox {
        galaxyShell(content)
        applyTheme(content.design?.theme)
    }

    val rightRoutes = content.galaxy.takeIf { it.isHost }?.toConfigRoute()?.let { listOf(it) }
    dock.mergeState(content.galaxy.toRoute(), RouteDockState(title = content.galaxy.name, rightRoutes = rightRoutes))

    // wireGalaxyMenu(root, content.galaxy)
}

fun RouteScope.viewGalaxyRoute() {
    routeBlock<GalaxyRoute, GalaxyContent>(GalaxyShell.islandId) { content ->
        viewGalaxy(content)
    }
}

// val hookInitializers: List<ViewScope.(HTMLElement) -> Unit> = listOf(
//     ViewScope::initPostMenu
// )
