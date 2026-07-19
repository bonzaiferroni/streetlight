package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationRoute
import streetlight.web.shells.LocationShell
import streetlight.web.shells.locationShell

fun RouteScope.viewLocation() {
    routeBlock<LocationRoute, LocationContent>(LocationShell.islandId) { location ->
        val element = shellBox(LocationShell.Id, hookInitializers) { // app.geoMap, app.appScope
            locationShell(location)
        }
    }
}

