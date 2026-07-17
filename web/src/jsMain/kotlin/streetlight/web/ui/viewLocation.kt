package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.LocationContent
import streetlight.model.ui.LocationRoute
import streetlight.web.shells.LocationProfileKey
import streetlight.web.shells.locationShell

fun ViewScope.viewLocation() {
    routeBlock<LocationRoute, LocationContent>(
        portal = portal,
        provideData = { api.readLocationContent(it.slug).handleResponse(toaster) }
    ) { location ->
        val element = shellBox(LocationProfileKey.Id, hookInitializers) { // app.geoMap, app.appScope
            locationShell(location)
        }
    }
}

