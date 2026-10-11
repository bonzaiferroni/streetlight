package streetlight.web.shell

import koala.modifier.Attribute
import koala.modifier.setAttribute
import kotlinx.html.FlowContent
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationRoute
import streetlight.web.layout.renderLayout
import streetlight.web.layout.mainBody
import koala.page.*

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
