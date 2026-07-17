package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import org.w3c.dom.HTMLElement
import streetlight.model.data.GalaxyContent
import streetlight.model.ui.GalaxyRoute
import streetlight.web.model.MarkerService
import streetlight.web.shells.GalaxyShell
import streetlight.web.shells.galaxyShell

fun ViewScope.viewGalaxy(content: GalaxyContent) {
    val markerService = app.get<MarkerService>()

    val root = shellBox(GalaxyShell.id, hookInitializers) {
        galaxyShell(content)
    }

    // wireLights(
    //     root = root,
    //     attribute = StarLightKey.EventLightId,
    //     cache = cache.eventLights
    // )
    wireGalaxyMenu(root, content.galaxy)

    val points = markerService.createMarkers(content.posts)
    markerMap.setPoints(points)
}

fun ViewScope.viewGalaxyRoute() {
    routeBlock<GalaxyRoute, GalaxyContent>(portal, { route ->
        readIsland<GalaxyContent>(GalaxyShell.islandId) { it.galaxy.slug == route.slug }
            ?: api.readGalaxyContent(route.slug).handleResponse(toaster)
    }) { content ->
        viewGalaxy(content)
    }
}

val hookInitializers: List<ViewScope.(HTMLElement) -> Unit> = listOf(
    ViewScope::initPostMenu
)