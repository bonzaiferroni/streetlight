package streetlight.web.ui

import koala.dom.*
import koala.dom.routeBlock
import streetlight.model.data.HomeContent
import streetlight.web.HomeRoute
import streetlight.web.io.getDataOrNull
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyContent
import streetlight.web.shells.GalaxyKey
import streetlight.web.shells.HomeKey
import streetlight.web.shells.homeShell

fun ViewContext<Streetlight>.viewHome(content: HomeContent) {
    val app = model

    val root = shellBox(HomeKey.ContainerId, app.geoMap, app.appScope) {
        homeShell(content)
    }

    // queryAndWireSwitch(root, Id("bruh"), onToggle = { console.log("bruh") })
    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = app.cache.eventLights
    )
    wireLights(
        root = root,
        attribute = StarLightKey.GalaxyLightId,
        cache = app.cache.galaxyLights,
    )
    wireLitEvents(root)
    wireGalaxyMenu(app, root, null)

    wireStreetMap()
}

fun ViewContext<Streetlight>.viewHomeRoute() {
    routeBlock<HomeRoute, HomeContent>(model.portal, { route ->
        readIslandOrApi(HomeKey.IslandId) {
            api.readHomeContent()?.getDataOrNull()
        }
    }) { content ->
        viewContextOf(model) {
            viewHome(content)
        }
    }
}