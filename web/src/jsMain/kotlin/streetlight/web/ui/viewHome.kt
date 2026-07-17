package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import koala.dom.routeBlock
import koala.model.Portal
import kotlinx.browser.document
import streetlight.model.data.HomeContent
import streetlight.model.ui.HomeRoute
import streetlight.web.io.ApiClient
import streetlight.web.model.DataCache
import streetlight.web.shells.HomeKey
import streetlight.web.shells.homeShell

fun ViewScope.viewHome(content: HomeContent) {
    val cache = app.get<DataCache>()

    val root = shellBoxWithMap(HomeKey.ContainerId, hookInitializers) {
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
    wireLitEvents(root)
    wireGalaxyMenu(root, null)

    wireStreetMap()

    document.setTitle(HomeRoute)
}

fun ViewScope.viewHomeRoute() {
    val portal = app.get<Portal>()
    val api = app.get<ApiClient>()

    routeBlock<HomeRoute, HomeContent>(portal, { _ ->
        readIsland(HomeKey.IslandId) { true }
            ?: api.readHomeContent().handleResponse(toaster)
    }) { content ->
        viewHome(content)
    }
}
