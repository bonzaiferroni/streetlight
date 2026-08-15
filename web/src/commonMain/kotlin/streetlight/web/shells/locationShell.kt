package streetlight.web.shells

import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationConfigRoute
import streetlight.model.ui.LocationRoute
import streetlight.web.layouts.renderLayout
import streetlight.web.pages.appFooter
import streetlight.web.ui.BodyStyle

fun FlowContent.locationShell(
    content: LocationContent,
) {
    val location = content.location
    val routeNow = LocationRoute(location.slug)
    column(LocationShell.shellId, BodyStyle.column) {
        setAttribute(Attribute.RoutePath, routeNow.toRelativePath())
        renderLayout(content)

        appFooter()

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