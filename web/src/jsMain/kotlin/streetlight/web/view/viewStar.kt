package streetlight.web.view

import streetlight.model.ui.ProfileConfigRoute
import streetlight.web.model.RouteDockState
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.routeBlock
import koala.dom.page.shellBox
import streetlight.model.data.StarContent
import streetlight.model.ui.StarRoute
import streetlight.web.shell.StarShell
import streetlight.web.shell.starShell
import streetlight.web.dom.page.applyTheme
import streetlight.web.model.dock

fun ViewScope.viewStar(content: StarContent) {
    shellBox {
        starShell(content)
        applyTheme(content.star.design?.theme)
    }

    val rightRoutes = content.takeIf { it.isCaller }?.let { listOf(ProfileConfigRoute) }
    dock.mergeState(StarRoute(content.star.username), RouteDockState(rightRoutes = rightRoutes))
}

fun RouteScope.viewStarRoute() {
    routeBlock<StarRoute, StarContent>(StarShell.islandId) { content ->
        viewStar(content)
    }
}
