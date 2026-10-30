package streetlight.web.view

import streetlight.model.ui.LocationConfigRoute
import koala.dom.*
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationRoute
import streetlight.web.shell.LocationShell
import streetlight.web.shell.locationShell
import koala.dom.page.*
import streetlight.web.dom.page.applyTheme

fun RouteScope.viewLocation() {
    routeBlock<LocationRoute, LocationContent>(LocationShell.islandId) { content ->
        val location = content.location
        shellBox {
            locationShell(content)
            applyTheme(content.design?.theme)
        }
    }
}
