package streetlight.web.ui

import koala.dom.*
import koala.dom.routeBlock
import kotlinx.browser.document
import streetlight.model.data.HomeContent
import streetlight.model.ui.HomeRoute
import streetlight.web.model.DataCache
import streetlight.web.shells.HomeShell
import streetlight.web.shells.homeShell

fun ViewScope.viewHome(content: HomeContent) {
    val cache = app.get<DataCache>()

    val root = shellBoxWithMap(HomeShell.ContainerId, hookInitializers) {
        homeShell(content)
    }

    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = cache.eventLights
    )
    wireLights(
        root = root,
        attribute = StarLightKey.GalaxyLightId,
        cache = cache.galaxyLights,
    )
    // wireLitEvents(root)
    wireGalaxyMenu(root, null)

    wireStreetMap()

    document.setTitle(HomeRoute)
}

fun RouteScope.viewHomeRoute() {
    routeBlock<HomeRoute, HomeContent>(HomeShell.IslandId) { content ->
        viewHome(content)
    }
}
