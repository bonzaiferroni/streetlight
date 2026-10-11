package streetlight.web.view

import kampfire.model.toDataOrNull
import koala.dom.*
import koala.dom.routeBlock
import streetlight.model.data.HomeContent
import streetlight.model.ui.HomeRoute
import streetlight.web.model.MarkerMap
import streetlight.web.shell.HomeShell
import streetlight.web.shell.homeShell
import web.dom.document
import koala.dom.page.*
import streetlight.web.model.api
import streetlight.web.dom.page.applyTheme
import streetlight.web.model.markerMap
import streetlight.web.utility.setTitle
import streetlight.web.model.toaster

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
