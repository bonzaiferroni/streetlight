package streetlight.web.ui

import koala.dom.*
import streetlight.model.data.CityListContent
import streetlight.model.ui.CityListRoute
import streetlight.web.shells.CityListShell
import streetlight.web.shells.cityListShell
import web.dom.document

fun ViewScope.viewCityList(content: CityListContent) {
    shellBox {
        cityListShell(content)
    }

    universeRouteMenu(CityListRoute)

    document.setTitle(CityListRoute)
    applyTheme(null)
}

fun RouteScope.viewCityListRoute() {
    routeBlock<CityListRoute, CityListContent>(CityListShell.IslandId) { content ->
        viewCityList(content)
    }
}
