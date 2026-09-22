package streetlight.web.ui

import koala.SvgFile
import koala.dom.MenuRoute
import koala.dom.ViewScope
import koala.dom.routeMenu
import koala.html.AppRoute
import koala.html.IconRoute
import streetlight.model.data.Galaxy
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.toConfigRoute
import streetlight.model.ui.toEarthRoute
import streetlight.model.ui.toRoute

fun ViewScope.galaxyRouteMenu(galaxy: Galaxy, routeNow: AppRoute) {
    routeMenu(
        context = galaxy.name,
        optionNow = MenuRoute(routeNow),
        options = listOf(MenuRoute(galaxy.toRoute()), MenuRoute(galaxy.toEarthRoute())),
        leftIcons = listOf(IconRoute(SvgFile.Home, HomeRoute)),
        rightIcons = listOf(IconRoute(SvgFile.GearSmall, galaxy.toConfigRoute()))
    )
}
