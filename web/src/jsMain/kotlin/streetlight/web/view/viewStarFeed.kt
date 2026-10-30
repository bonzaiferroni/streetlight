package streetlight.web.view

import koala.dom.*
import koala.dom.page.*
import streetlight.model.data.StarFeedContent
import streetlight.model.ui.StarFeedRoute
import streetlight.web.dom.page.applyTheme
import streetlight.web.shell.StarFeedShell
import streetlight.web.shell.starFeedShell
import streetlight.web.utility.setTitle
import web.dom.document

fun ViewScope.viewStarFeed(content: StarFeedContent) {
    shellBox {
        starFeedShell(content)
    }

    document.setTitle(StarFeedRoute())
    applyTheme(null)
}

fun RouteScope.viewStarFeedRoute() {
    routeBlock<StarFeedRoute, StarFeedContent>(StarFeedShell.IslandId) { content ->
        viewStarFeed(content)
    }
}
