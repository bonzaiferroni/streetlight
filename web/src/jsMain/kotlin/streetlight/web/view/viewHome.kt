package streetlight.web.view

import kampfire.model.toDataOrNull
import koala.dom.*
import koala.dom.routeBlock
import streetlight.model.data.HomeContent
import streetlight.model.ui.HomeRoute
import koala.model.MarkerMap
import streetlight.web.shell.HomeShell
import streetlight.web.shell.homeShell
import web.dom.document
import koala.dom.page.*
import streetlight.web.model.api
import streetlight.web.dom.page.applyTheme
import streetlight.web.model.geoMap
import streetlight.web.model.setPoints
import streetlight.web.utility.setTitle
import streetlight.web.model.toaster

fun ViewScope.viewHome(content: HomeContent) {
    val markerMap = MarkerMap(geoMap)

    shellBoxWithMap(markerMap) {
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
