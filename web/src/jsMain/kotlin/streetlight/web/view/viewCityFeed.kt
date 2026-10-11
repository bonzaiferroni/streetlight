package streetlight.web.view

import koala.dom.*
import koala.dom.page.*
import streetlight.model.data.CityFeedContent
import streetlight.model.ui.CityFeedRoute
import streetlight.web.dom.page.applyTheme
import streetlight.web.shell.CityFeedShell
import streetlight.web.shell.cityFeedShell
import streetlight.web.utility.setTitle
import web.dom.document

fun ViewScope.viewCityFeed(content: CityFeedContent) {
    shellBox {
        cityFeedShell(content)
    }

    document.setTitle(CityFeedRoute())
    applyTheme(null)
}

fun RouteScope.viewCityFeedRoute() {
    routeBlock<CityFeedRoute, CityFeedContent>(CityFeedShell.IslandId) { content ->
        viewCityFeed(content)
    }
}
