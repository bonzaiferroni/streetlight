package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.dom.*
import streetlight.model.data.LocationContent
import streetlight.web.LocationRoute
import streetlight.web.shells.LocationProfileKey
import streetlight.web.shells.locationShell

fun AppScope.viewLocation() {
    routeBlock<LocationRoute, LocationContent>(
        portal = portal,
        provideData = { api.readLocationContent(it.slug).handleOutcome(toaster::toast) }
    ) { location ->
        val element = shellBox(LocationProfileKey.Id, hookInitializers) { // app.geoMap, app.appScope
            locationShell(location)
        }
    }
}

