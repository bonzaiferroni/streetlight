package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.topLogo
import streetlight.model.data.LocationConfig
import streetlight.model.data.toEdit
import streetlight.model.ui.LocationConfigRoute
import streetlight.web.pages.appFooter
import streetlight.web.shells.cardOf

fun ViewScope.viewLocationConfig(
    config: LocationConfig,
) {
    val location = config.location
    column(BodyStyle.Column) {
        topLogo()
        cardOf(location)
        tabs(Id("location-config-tabs")) {
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
            tab("automate") {
                viewLocationAutomation(config)
            }
        }
        appFooter()
    }
}

fun RouteScope.viewLocationConfigRoute() {
    routeBlock<LocationConfigRoute, LocationConfig> { config ->
        viewLocationConfig(config)
    }
}