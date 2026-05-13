package streetlight.web.ui

import koala.dom.*
import koala.dom.routeBlock
import koala.model.GeoMap
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.HomeContent
import streetlight.web.HomeRoute
import streetlight.web.io.ApiClient
import streetlight.web.io.getDataOrNull
import streetlight.web.model.DataCache
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyContent
import streetlight.web.shells.GalaxyKey
import streetlight.web.shells.HomeKey
import streetlight.web.shells.homeShell

fun RenderContext.viewHome(content: HomeContent) {
    val geoMap = app.get<GeoMap>()
    val appScope = app.get<CoroutineScope>()
    val cache = app.get<DataCache>()

    val root = shellBox(HomeKey.ContainerId, geoMap, appScope) {
        homeShell(content)
    }

    // queryAndWireSwitch(root, Id("bruh"), onToggle = { console.log("bruh") })
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
}

fun RenderContext.viewHomeRoute() {
    val portal = app.get<Portal>()
    val api = app.get<ApiClient>()

    routeBlock<HomeRoute, HomeContent>(portal, { route ->
        readIslandOrApi(HomeKey.IslandId, { true }) {
            api.readHomeContent()?.getDataOrNull()
        }
    }) { content ->
        viewHome(content)
    }
}