package streetlight.web.ui

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.routeBlock
import koala.dom.shellBox
import streetlight.model.data.StarContent
import streetlight.model.ui.StarRoute
import streetlight.web.shells.StarShell
import streetlight.web.shells.starShell

fun ViewScope.viewStar(content: StarContent) {
    shellBox(StarShell.id) {
        starShell(content)
    }
}

fun RouteScope.viewStarRoute() {
    routeBlock<StarRoute, StarContent>(StarShell.islandId) { content ->
        viewStar(content)
    }
}