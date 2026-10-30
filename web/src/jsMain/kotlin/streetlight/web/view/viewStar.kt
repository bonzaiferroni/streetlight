package streetlight.web.view

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.routeBlock
import koala.dom.page.shellBox
import streetlight.model.data.StarContent
import streetlight.model.ui.StarRoute
import streetlight.web.shell.StarShell
import streetlight.web.shell.starShell
import streetlight.web.dom.page.applyTheme

fun ViewScope.viewStar(content: StarContent) {
    shellBox {
        starShell(content)
        applyTheme(content.star.design?.theme)
    }

}

fun RouteScope.viewStarRoute() {
    routeBlock<StarRoute, StarContent>(StarShell.islandId) { content ->
        viewStar(content)
    }
}
