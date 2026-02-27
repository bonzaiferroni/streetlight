package streetlight.web

import koala.css.*
import koala.dom.*
import streetlight.model.data.Location
import streetlight.model.data.toEdit

fun RenderContext.locationAdminView(
    app: AppContext,
    location: Location,
) {
    column {
        tabs {
            tab("profile") {
                val edit = location.toEdit()
                locationEditorView(edit, app)
            }
            tab("events") {
                column {
                    location.eventsLink.let { link ->
                        row {
                            textBlock("This location has an event page that we can try to read. " +
                                    "The last time it was checked was ${location.checkedAt}.", modify(Flex1))
                            button("read events", onClick = {
                                app.portal.go(ReadEventRoute(location, link))
                            })
                        }
                    }
                    textBlock("yer events")
                }
            }
        }
        appFooter()
    }
}

fun RenderContext.locationAdminRouteView(app: AppContext) {
    routeBlock<LocationAdminRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) {
        locationAdminView(app, it)
    }
}