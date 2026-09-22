package streetlight.web.ui

import koala.SvgFile
import koala.dom.MenuRoute
import koala.dom.ViewScope
import koala.dom.routeMenu
import koala.html.AppRoute
import koala.html.IconRoute
import streetlight.model.data.Location
import streetlight.model.ui.LocationConfigRoute

fun ViewScope.locationRouteMenu(location: Location, routeNow: AppRoute, canEdit: Boolean) {
    val rightIcons = when (canEdit) {
        true -> listOf(IconRoute(SvgFile.GearSmall, LocationConfigRoute(location.locationId)))
        else -> null
    }

    routeMenu(
        context = location.name ?: "Location",
        optionNow = MenuRoute(routeNow),
        options = listOf(MenuRoute(routeNow)),
        rightIcons = rightIcons
    )
}
