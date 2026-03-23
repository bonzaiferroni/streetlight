package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.flowerOf
import streetlight.model.data.Location
import streetlight.model.data.toEdit
import streetlight.web.LocationAdminRoute
import streetlight.web.OldEventScoutRoute
import streetlight.web.model.Streetlight
import streetlight.web.pages.appFooter
import streetlight.web.shells.cardOf

fun RenderContext.viewLocationAdmin(
    app: Streetlight,
    location: Location,
) {
    column {
        cardOf(location)
        tabs {
            tab("profile") {
                val edit = location.toEdit()
                viewLocationEditor(edit, app, null, null)
            }
            tab("events") {
                val events = flowerOf { app.client.api.readLocationEvents(location.locationId) }
                column {
                    location.eventsUrl.let { link ->
                        row {
                            textBlock("This location has an event page that we can try to read.", modify(Flex1))
                            button("read events", onClick = {
                                app.portal.go(OldEventScoutRoute(location, link))
                            })
                        }
                    }
                    flowBlock(events.flow, defaultMagic, magic = true) { events ->
                        column {
                            events.forEach { event ->
                                cardOf(event)
                            }
                        }
                    }
                }
            }
        }
        appFooter()
    }
}

fun RenderContext.viewLocationAdmin(app: Streetlight) {
    routeBlock<LocationAdminRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) {
        viewLocationAdmin(app, it)
    }
}