package streetlight.web.view

import koala.modifier.Accent
import koala.dom.*
import koala.model.MenuAction
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationUpdaterContent
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.LocationUpdateRoute
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*
import streetlight.web.dom.layout.configBody
import streetlight.web.dom.component.configHeading
import streetlight.web.dom.layout.deltaRow
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.getLocationEditor
import streetlight.web.model.portal

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

fun RouteScope.viewLocationUpdaterRoute() {
    routeBlock<LocationUpdateRoute, LocationUpdaterContent> { content ->
        starGate { star ->
            viewLocationUpdater(content, star)
        }
    }
}