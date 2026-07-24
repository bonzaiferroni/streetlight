package streetlight.web.shells

import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationAdminRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.LocationUpdateRoute
import streetlight.web.layouts.layoutPosts
import streetlight.web.layouts.postRow
import streetlight.web.pages.appFooter
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.headerOf

fun FlowContent.locationShell(
    content: LocationContent,
) {
    val location = content.location
    column(LocationShell.Id, BodyStyle.Column) {
        headerOf(
            location = location,
            editRoute = if (content.canEdit) LocationUpdateRoute(location.slug) else null
        )

        tabs(LocationShell.tabsId) {
            if (content.events.isNotEmpty()) {
                tab("events") {
                    layoutPosts {
                        content.events.forEach {
                            postRow(it)
                        }
                    }
                }
            }
            tab("directions") {
                textBlock("yer directions")
            }
            tab("menu") {
                textBlock("yer menu")
            }
            tab("talk") {
                textBlock("yer talk")
            }
        }

        appFooter()

        val routeNow = LocationRoute(location.slug)
        val adminRoute = content.takeIf { it.canEdit }?.let { LocationAdminRoute(location.locationId) }
        routeMenu(location.name ?: "Location", routeNow, listOf(routeNow, adminRoute))
    }

    dataIsland(LocationShell.islandId, content)
}

object LocationShell {
    val Id = Id("location-shell")
    val tabsId = Id("location-tabs")
    val adminCard = Id("location-admin-card")
    val islandId = Id("location-island")
}