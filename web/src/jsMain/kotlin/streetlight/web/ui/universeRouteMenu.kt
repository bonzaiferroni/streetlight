package streetlight.web.ui

import koala.SvgFile
import koala.dom.MenuRoute
import koala.dom.ViewScope
import koala.dom.routeMenu
import koala.html.AppRoute
import koala.html.IconRoute
import streetlight.model.ui.CityListRoute
import streetlight.model.ui.CityMapRoute
import streetlight.model.ui.GalaxyListRoute
import streetlight.model.ui.GalaxyMapRoute
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.PostMapRoute

fun ViewScope.universeRouteMenu(routeNow: AppRoute) {
    val mapRoute = when (routeNow) {
        is CityListRoute -> CityMapRoute(null)
        is GalaxyListRoute -> GalaxyMapRoute(null)
        else -> PostMapRoute()
    }

    routeMenu(
        context = "Streetlight",
        optionNow = MenuRoute(routeNow),
        options = listOf(MenuRoute(HomeRoute), MenuRoute(GalaxyListRoute), MenuRoute(CityListRoute)),
        rightIcons = listOf(IconRoute(SvgFile.Earth, mapRoute))
    )
}
