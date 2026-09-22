package streetlight.web.ui

import koala.SvgFile
import koala.dom.MenuRoute
import koala.dom.ViewScope
import koala.dom.routeMenu
import koala.html.AppRoute
import koala.html.IconRoute
import streetlight.model.data.Star
import streetlight.model.ui.ProfileConfigRoute
import streetlight.model.ui.StarRoute

fun ViewScope.starRouteMenu(star: Star, routeNow: AppRoute, isCaller: Boolean) {
    val options = listOf(MenuRoute(StarRoute(star.username)))

    val rightIcons = when (isCaller) {
        true -> listOf(IconRoute(SvgFile.GearSmall, ProfileConfigRoute))
        else -> null
    }

    routeMenu(
        context = star.username.value,
        optionNow = MenuRoute(routeNow),
        options = options,
        rightIcons = rightIcons
    )
}
