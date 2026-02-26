package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.button
import koala.html.geoMapMount
import koala.html.heading1
import koala.html.image
import koala.html.propertyValue
import koala.utils.prettyPrint
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.toLocationEdit
import streetlight.web.shells.locationShell

fun RenderContext.locationProfileView(app: AppContext) {
    routeBlock<LocationProfileRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) { location ->
        column {
            shellBox(Id("location-profile"), app.geoMap, app.appScope) {
                locationShell(location)
            }
            location.link?.let {
                button("check", onClick = {
                    renderScope.launch {
                        app.client.api.parseLocation(it)?.toLocationEdit(location.locationId)?.let { edit ->
                            app.portal.go(EditLocationDataRoute(edit))
                        }
                    }
                })
            }
        }
    }
}

