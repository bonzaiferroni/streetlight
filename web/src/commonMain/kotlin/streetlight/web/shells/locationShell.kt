package streetlight.web.shells

import koala.modifier.Attribute
import koala.modifier.setAttribute
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationRoute
import streetlight.web.layouts.renderLayout
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader
import streetlight.web.ui.BodyStyle

fun FlowContent.locationShell(
    content: LocationContent,
) {
    val location = content.location
    val routeNow = LocationRoute(location.slug)
    column(BodyStyle.ShellColumn) {
        appHeader()

        column(BodyStyle.MainColumn) {
            setAttribute(Attribute.RoutePath, routeNow.toRelativePath())
            renderLayout(content)

            appFooter()
        }
    }

    dataIsland(LocationShell.islandId, content)
}

object LocationShell {
    val islandId = Id("location-island")
}
