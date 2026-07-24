package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.model.data.Location
import streetlight.model.data.toEdit
import streetlight.model.ui.LocationAdminRoute
import streetlight.web.pages.appFooter
import streetlight.web.shells.cardOf

fun ViewScope.viewLocationAdmin(
    location: Location,
) {
    column(BodyStyle.Column) {
        cardOf(location)
        tabs {
            tab("profile") {
                val edit = location.toEdit()
                // viewLocationEditor(edit, app, null, false, null)
            }
            tab("events") {
                column {
                    location.eventsUrl.let { link ->
                        row {
                            textBlock("This location has an event page that we can try to read.", modify(Flex1))
                            button("read events", onClick = {
                                // app.portal.go(OldEventScoutRoute(location, link))
                            })
                        }
                    }
                    dataBlock({ api.readLocationEvents(location.slug) }) { events ->
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

fun RouteScope.viewLocationAdmin() {
    routeBlock<LocationAdminRoute, Location> {
        viewLocationAdmin(it)
    }
}