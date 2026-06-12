package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.Location
import streetlight.web.LocationRoute
import streetlight.web.shells.LocationProfileKey
import streetlight.web.shells.locationShell

fun AppScope.viewLocationProfile() {
    routeBlock<LocationRoute, Location>(
        portal = portal,
        provideData = { api.readLocationSlug(it.slug).handleResponse(toaster::toast) }
    ) { location ->
        val element = shellBox(LocationProfileKey.Id) { // app.geoMap, app.appScope
            locationShell(location)
        }
    }
}

