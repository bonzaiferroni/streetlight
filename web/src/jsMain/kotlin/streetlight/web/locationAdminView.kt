package streetlight.web

import koala.dom.*
import streetlight.model.data.Location

fun RenderContext.locationAdminView(
    app: AppContext,
    location: Location,
) {
    tabs()
}

fun RenderContext.locationAdminRouteView(app: AppContext) {
    routeBlock<LocationAdminRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) {
        locationAdminView(app, it)
    }
}