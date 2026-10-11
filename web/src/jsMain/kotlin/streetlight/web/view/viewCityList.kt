package streetlight.web.view

import koala.dom.*
import streetlight.model.data.CityListContent
import streetlight.model.ui.CityListRoute
import streetlight.web.shell.CityListShell
import streetlight.web.shell.cityListShell
import web.dom.document
import koala.dom.page.*
import streetlight.web.dom.page.applyTheme
import streetlight.web.utility.setTitle

fun ViewScope.viewCityList(content: CityListContent) {
    shellBox {
        cityListShell(content)
    }

    document.setTitle(CityListRoute)
    applyTheme(null)
}

fun RouteScope.viewCityListRoute() {
    routeBlock<CityListRoute, CityListContent>(CityListShell.IslandId) { content ->
        viewCityList(content)
    }
}
