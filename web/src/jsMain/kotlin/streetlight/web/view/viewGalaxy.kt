package streetlight.web.view

import streetlight.model.ui.toRoute
import koala.dom.*
import streetlight.model.data.GalaxyContent
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.toConfigRoute
import streetlight.web.shell.GalaxyShell
import streetlight.web.shell.galaxyShell
import koala.dom.page.*
import streetlight.web.dom.page.applyTheme
import streetlight.web.interop.wireGalaxyMenu

fun ViewScope.viewGalaxy(content: GalaxyContent) {
    shellBox {
        galaxyShell(content)
        applyTheme(content.design?.theme)
    }


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
