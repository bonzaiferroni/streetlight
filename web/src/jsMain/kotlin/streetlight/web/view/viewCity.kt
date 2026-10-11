package streetlight.web.view

import kampfire.model.toDataOrNull
import kampfire.model.reactIn
import koala.dom.*
import koala.model.withLabel
import streetlight.model.data.CityContent
import streetlight.model.ui.CityConfigRoute
import streetlight.model.ui.CityRoute
import streetlight.model.ui.EventsMapRoute
import streetlight.web.model.RouteDockState
import streetlight.web.shell.CityShell
import streetlight.web.shell.cityShell
import koala.dom.page.*
import streetlight.web.model.api
import streetlight.web.dom.page.applyTheme
import streetlight.web.model.dock
import streetlight.web.model.session
import streetlight.web.model.toaster

fun ViewScope.viewCity(content: CityContent) {
    shellBox {
        cityShell(content)
    }

    applyTheme(null)

    followFeedRoute<CityRoute>({ CityRoute(content.city.slug, it) }) { route ->
        api.city.readCityContent(route.slug, route.feed).toDataOrNull(toaster) { it.feed }
    }

    // any signed-in user may edit a city
    val slug = content.city.slug
    val mapRoute = EventsMapRoute(content.city.geoRect)
    val cityRoute = CityRoute(slug)
    val mainRoutes = listOf(cityRoute.withLabel("Feed"), mapRoute.withLabel("Map"))
    session.starState.reactIn(contentScope) { star ->
        val rightRoutes = star?.let { listOf(CityConfigRoute(slug)) } ?: emptyList()
        dock.mergeState(cityRoute, RouteDockState(mainRoutes, content.city.name, rightRoutes = rightRoutes))
    }
}

fun RouteScope.viewCityRoute() {
    routeBlock<CityRoute, CityContent>(CityShell.IslandId) { content ->
        viewCity(content)
    }
}
