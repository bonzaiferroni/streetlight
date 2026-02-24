package streetlight.web

import koala.css.MarginAuto
import koala.css.MaxHeight64
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.column
import koala.dom.routeBlock
import koala.html.button
import koala.html.heading1
import koala.html.image
import streetlight.model.data.Location

fun RenderContext.locationProfileView(app: AppContext) {
    routeBlock<LocationProfileRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) { location ->
        column {
            location.imageUrl?.let {
                image(it, modify(MaxHeight64, MarginAuto))
            }
            heading1(location.name)
            button("Edit", EditLocationIdRoute(location.locationId))
        }
    }
}