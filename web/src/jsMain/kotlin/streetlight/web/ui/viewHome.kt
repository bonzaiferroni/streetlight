package streetlight.web.ui

import koala.dom.*
import koala.dom.routeBlock
import kotlinx.browser.document
import kotlinx.coroutines.delay
import streetlight.model.data.HomeContent
import streetlight.model.ui.HomeRoute
import streetlight.web.model.DataCache
import streetlight.web.shells.HomeShell
import streetlight.web.shells.homeShell
import kotlin.time.Duration.Companion.seconds

fun ViewScope.viewHome(content: HomeContent) {
    val cache = app.get<DataCache>()

    val root = shellBoxWithMap(HomeShell.ContainerId) {
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
    applyTheme(null)

    launchEffect {
        toaster.deliver("hello")
        delay(1.seconds)
        toaster.deliver("hello again")
        delay(1.seconds)
        toaster.deliver("this is the last hello")
    }
}

fun RouteScope.viewHomeRoute() {
    routeBlock<HomeRoute, HomeContent>(HomeShell.IslandId) { content ->
        viewHome(content)
    }
}
