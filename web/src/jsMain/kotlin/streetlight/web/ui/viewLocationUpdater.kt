package streetlight.web.ui

import koala.modifier.Accent
import koala.dom.*
import koala.dom.MenuAction
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationUpdaterContent
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.LocationUpdateRoute

fun ViewScope.viewLocationUpdater(content: LocationUpdaterContent, star: Star) {
    val edit = content.location.toEdit()
    val model = edit.let { app.getLocationEditor(it, contentScope) }

    configBody("Location", "Config", "viewLocationUpdater.kt") {
        configHeading(content.location.name ?: "Location", LocationRoute(content.location.slug))

        lazyTabs {
            tab("edit") {
                column {
                    locationEditFormBody(model)
                    formSubmit(
                        label = "Save",
                        onClick = {
                            launchEffect(::viewLocationUpdater) {
                                val location = model.submitSuspend()
                                if (location != null) {
                                    portal.go(LocationRoute(location.slug))
                                }
                            }
                        },
                        messenger = model.messages,
                        buttonMod = Accent,
                        back = MenuAction("go back", onClick = portal::goBack)
                    )
                }
            }
            tab("history") {
                viewEditHistory<LocationEdit>(content.editLogs) { edit, compareEdit ->
                    deltaRow("name", edit.name, compareEdit?.name)
                    deltaRow("address", edit.address, compareEdit?.address)
                    deltaRow("description", edit.description, compareEdit?.description)
                    deltaRow("geolocation", edit.geoPoint, compareEdit?.geoPoint)
                }
            }
        }
    }
}

fun RouteScope.viewUpdateLocationRoute() {
    routeBlock<LocationUpdateRoute, LocationUpdaterContent> { content ->
        starGate { star ->
            viewLocationUpdater(content, star)
        }
    }
}