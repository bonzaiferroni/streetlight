package streetlight.web.ui

import koala.dom.*
import streetlight.model.data.Location
import streetlight.web.LocationIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.LocationProfileKey
import streetlight.web.shells.locationShell

fun RenderContext.viewLocationProfile(app: Streetlight) {
    routeBlock<LocationIdRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) { location ->
        val element = shellBox(LocationProfileKey.Id) { // app.geoMap, app.appScope
            locationShell(location)
        }
    }
}

