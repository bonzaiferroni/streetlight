package streetlight.web.ui

import koala.dom.*
import koala.dom.routeBlock
import streetlight.model.data.HomeContent
import streetlight.model.ui.HomeRoute
import streetlight.web.model.MarkerMap
import streetlight.web.shells.HomeShell
import streetlight.web.shells.homeShell
import web.dom.document

fun ViewScope.viewHome(content: HomeContent) {
    val markerMap = app.get<MarkerMap>()

    shellBoxWithMap {
        homeShell(content)
    }

    markerMap.setPoints(content.feed.entities)
    document.setTitle(HomeRoute)
    applyTheme(null)
}

fun RouteScope.viewHomeRoute() {
    routeBlock<HomeRoute, HomeContent>(HomeShell.IslandId) { content ->
        viewHome(content)
    }
}
