package streetlight.web.ui

import koala.dom.*
import streetlight.model.data.CityContent
import streetlight.model.ui.CityRoute
import streetlight.web.shells.CityShell
import streetlight.web.shells.cityShell

fun ViewScope.viewCity(content: CityContent) {
    shellBox {
        cityShell(content)
    }

    applyTheme(null)
}

fun RouteScope.viewCityRoute() {
    routeBlock<CityRoute, CityContent>(CityShell.IslandId) { content ->
        viewCity(content)
    }
}
