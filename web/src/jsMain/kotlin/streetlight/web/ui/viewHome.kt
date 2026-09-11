package streetlight.web.ui

import koala.dom.*
import koala.dom.routeBlock
import streetlight.model.data.HomeContent
import streetlight.model.ui.HomeRoute
import streetlight.web.shells.HomeShell
import streetlight.web.shells.homeShell
import web.dom.document

fun ViewScope.viewHome(content: HomeContent) {

    val root = shellBoxWithMap(HomeShell.ContainerId) {
        homeShell(content)
    }

//    wireGalaxyMenu(root, null)

    wireStreetMap(content.feed)

    document.setTitle(HomeRoute)
    applyTheme(null)
}

fun RouteScope.viewHomeRoute() {
    routeBlock<HomeRoute, HomeContent>(HomeShell.IslandId) { content ->
        viewHome(content)
    }
}
