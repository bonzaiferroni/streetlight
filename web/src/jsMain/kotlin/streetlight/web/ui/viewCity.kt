package streetlight.web.ui

import kampfire.model.reactIn
import koala.dom.*
import streetlight.model.data.CityContent
import streetlight.model.ui.CityConfigRoute
import streetlight.model.ui.CityRoute
import streetlight.web.model.RouteDockState
import streetlight.web.shells.CityShell
import streetlight.web.shells.cityShell

fun ViewScope.viewCity(content: CityContent) {
    shellBox {
        cityShell(content)
    }

    applyTheme(null)

    // any signed-in user may edit a city
    val slug = content.city.slug
    session.starState.reactIn(contentScope) { star ->
        val rightRoutes = star?.let { listOf(CityConfigRoute(slug)) } ?: emptyList()
        dock.mergeState(CityRoute(slug), RouteDockState(title = content.city.name, rightRoutes = rightRoutes))
    }
}

fun RouteScope.viewCityRoute() {
    routeBlock<CityRoute, CityContent>(CityShell.IslandId) { content ->
        viewCity(content)
    }
}
