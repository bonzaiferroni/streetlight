package streetlight.web.ui

import koala.dom.*
import org.w3c.dom.HTMLElement
import streetlight.model.data.GalaxyContent
import streetlight.model.ui.GalaxyRoute
import streetlight.web.model.MarkerService
import streetlight.web.shells.GalaxyShell
import streetlight.web.shells.galaxyShell

fun ViewScope.viewGalaxy(content: GalaxyContent) {
    val markerService = app.get<MarkerService>()

    val root = shellBox(GalaxyShell.id) {
        galaxyShell(content)
    }

    wireGalaxyMenu(root, content.galaxy)

    val points = markerService.createMarkers(content.posts)
    markerMap.setPoints(points)
}

fun RouteScope.viewGalaxyRoute() {
    routeBlock<GalaxyRoute, GalaxyContent>(GalaxyShell.islandId) { content ->
        viewGalaxy(content)
    }
}

// val hookInitializers: List<ViewScope.(HTMLElement) -> Unit> = listOf(
//     ViewScope::initPostMenu
// )