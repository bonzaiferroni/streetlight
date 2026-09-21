package streetlight.web.shells

import koala.SvgFile
import koala.html.AppRoute
import koala.html.IconRoute
import koala.html.routeMenu
import kotlinx.html.FlowContent
import streetlight.model.ui.CityListRoute
import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.GalaxyListRoute
import streetlight.model.ui.GalaxyMapRoute
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.PostMapRoute

fun FlowContent.universeRouteMenu(routeNow: AppRoute) {
    val mapRoute = when (routeNow) {
        is CityListRoute -> CityMapRoute(null)
        is GalaxyListRoute -> GalaxyMapRoute(null)
        else -> PostMapRoute()
    }

    routeMenu(
        context = "Streetlight",
        routeNow = routeNow,
        routes = listOf(HomeRoute, GalaxyListRoute, CityListRoute),
        rightIcons = listOf(IconRoute(SvgFile.Earth, mapRoute))
    )
}
