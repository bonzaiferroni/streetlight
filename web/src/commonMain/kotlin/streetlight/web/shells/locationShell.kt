package streetlight.web.shells

import koala.modifier.Attribute
import koala.modifier.setAttribute
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationRoute
import streetlight.web.layouts.renderLayout
import streetlight.web.ui.mainBody

fun FlowContent.locationShell(
    content: LocationContent,
) {
    val location = content.location
    val routeNow = LocationRoute(location.slug)
    mainBody("locationShell.kt") {
        setAttribute(Attribute.RoutePath, routeNow.toRelativePath())
        renderLayout(content)
    }

    dataIsland(LocationShell.islandId, content)
}

object LocationShell {
    val islandId = Id("location-island")
}
