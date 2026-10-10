package streetlight.web.ui

import kampfire.model.toDataOrNull
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
    document.setTitle(HomeRoute())
    applyTheme(null)

    followFeedRoute<HomeRoute>({ HomeRoute(it) }, { markerMap.setPoints(it.entities) }) { route ->
        api.content.readHomeContent(route.feed).toDataOrNull(toaster) { it.feed }
    }
}

fun RouteScope.viewHomeRoute() {
    routeBlock<HomeRoute, HomeContent>(HomeShell.IslandId) { content ->
        viewHome(content)
    }
}
