package streetlight.web

import koala.css.*
import koala.dom.*
import koala.model.flowerOf
import streetlight.model.data.Location
import streetlight.model.data.toEdit
import streetlight.web.shells.cardOf

fun RenderContext.viewLocationAdmin(
    app: AppContext,
    location: Location,
) {
    column {
        cardOf(location)
        tabs {
            tab("profile") {
                val edit = location.toEdit()
                locationEditorView(edit, app)
            }
            tab("events") {
                val events = flowerOf { app.client.api.readLocationEvents(location.locationId) }
                column {
                    location.eventsLink.let { link ->
                        row {
                            val lastTime = location.checkedAt?.let { "The last time it was checked was ${it}." } ?: ""
                            textBlock("This location has an event page that we can try to read. $lastTime", modify(Flex1))
                            button("read events", onClick = {
                                app.portal.go(ReadEventRoute(location, link))
                            })
                        }
                    }
                    flowBlock(events.flow, modify(Blur, SlideX), animate = true) { events ->
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

fun RenderContext.viewLocationAdmin(app: AppContext) {
    routeBlock<LocationAdminRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) {
        viewLocationAdmin(app, it)
    }
}