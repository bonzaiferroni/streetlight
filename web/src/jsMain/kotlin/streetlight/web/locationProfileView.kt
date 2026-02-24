package streetlight.web

import koala.dom.RenderContext
import koala.dom.column
import koala.dom.routeBlock
import koala.html.button
import koala.html.heading1
import streetlight.model.data.Location

fun RenderContext.locationProfileView(app: AppContext) {
    routeBlock<LocationProfileRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) { location ->
        column {
            heading1(location.name)
            button("Edit", EditLocationIdRoute(location.locationId))
        }
    }
}