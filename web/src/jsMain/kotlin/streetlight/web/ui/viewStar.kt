package streetlight.web.ui

import streetlight.model.ui.ProfileConfigRoute
import streetlight.web.model.RouteDockState
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.routeBlock
import koala.dom.shellBox
import streetlight.model.data.StarContent
import streetlight.model.ui.StarRoute
import streetlight.web.shells.StarShell
import streetlight.web.shells.starShell

fun ViewScope.viewStar(content: StarContent) {
    shellBox {
        starShell(content)
        applyTheme(content.star.design?.theme)
    }

    val rightRoutes = content.takeIf { it.isCaller }?.let { listOf(ProfileConfigRoute) }
    dock.mergeState(RouteDockState(rightRoutes = rightRoutes))
}

fun RouteScope.viewStarRoute() {
    routeBlock<StarRoute, StarContent>(StarShell.islandId) { content ->
        viewStar(content)
    }
}
