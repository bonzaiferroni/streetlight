package streetlight.web.ui

import streetlight.model.ui.LocationConfigRoute
import streetlight.web.model.RouteDockState
import koala.dom.*
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationRoute
import streetlight.web.shells.LocationShell
import streetlight.web.shells.locationShell

fun RouteScope.viewLocation() {
    routeBlock<LocationRoute, LocationContent>(LocationShell.islandId) { content ->
        val location = content.location
        shellBox {
            locationShell(content)
            applyTheme(content.design?.theme)
        }

        val rightRoutes = content.takeIf { it.canEdit }?.let { listOf(LocationConfigRoute(location.locationId)) }
        dock.mergeState(RouteDockState(title = location.name, rightRoutes = rightRoutes))
    }
}
