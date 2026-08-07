package streetlight.web.shells

import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationConfigRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.LocationUpdateRoute
import streetlight.web.layouts.buildLayout
import streetlight.web.layouts.layoutPosts
import streetlight.web.layouts.postRow
import streetlight.web.pages.appFooter
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.headerOf

fun FlowContent.locationShell(
    content: LocationContent,
) {
    val location = content.location
    column(LocationShell.shellId, BodyStyle.column) {
        buildLayout(content)

        val routeNow = LocationRoute(location.slug)
        val adminRoute = content.takeIf { it.canEdit }?.let { LocationConfigRoute(location.locationId) }
        routeMenu(location.name ?: "Location", routeNow, listOf(routeNow, adminRoute))
    }

    dataIsland(LocationShell.islandId, content)
}

object LocationShell {
    val shellId = Id("location-shell")
    val tabsId = Id("location-tabs")
    val adminCard = Id("location-admin-card")
    val islandId = Id("location-island")
}