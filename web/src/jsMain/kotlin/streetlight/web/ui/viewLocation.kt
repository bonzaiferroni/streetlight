package streetlight.web.ui

import koala.dom.*
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationRoute
import streetlight.web.shells.LocationShell
import streetlight.web.shells.locationShell

fun RouteScope.viewLocation() {
    routeBlock<LocationRoute, LocationContent>(LocationShell.islandId) { location ->
        shellBox {
            locationShell(location)
            applyTheme(location.design?.theme)
        }
    }
}

