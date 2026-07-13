package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.dom.AppScope
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.shellBox
import koala.dom.textBlock
import streetlight.model.data.StarContent
import streetlight.web.StarRoute
import streetlight.web.shells.GalaxyShell
import streetlight.web.shells.StarShell
import streetlight.web.shells.starShell

fun AppScope.viewStar(content: StarContent) {
    shellBox(StarShell.id, hookInitializers) {
        starShell(content)
    }
}

fun AppScope.viewStarRoute() {
    routeBlock<StarRoute, StarContent>(portal, { route ->
        readIsland<StarContent>(StarShell.islandId) { it.star.username == route.username } ?:
            api.readStarContent(route.username).handleOutcome(toaster::toast)
    }) { content ->
        viewStar(content)
    }
}