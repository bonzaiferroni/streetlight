package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.Location
import streetlight.web.LocationIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.LocationProfileKey
import streetlight.web.shells.locationShell

fun RenderContext.viewLocationProfile() {
    routeBlock<LocationIdRoute, Location>(
        portal = portal,
        provideData = { api.readLocation(it.locationId).handleResponse(toaster::toast) }
    ) { location ->
        val element = shellBox(LocationProfileKey.Id) { // app.geoMap, app.appScope
            locationShell(location)
        }
    }
}

