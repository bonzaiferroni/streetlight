package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationUpdaterContent
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.LocationUpdateRoute

fun ViewScope.viewLocationUpdater(content: LocationUpdaterContent, star: Star) {
    val edit = content.location.toEdit()
    val model = edit.let { app.getLocationEditor(it, contentScope) }

    tabs {
        tab("edit") {
            column {
                updaterGreeting(star, model.stateNow.edit.name ?: "this location")
                locationEditFormBody(model)
                formSubmit(
                    label = "Save",
                    onSubmit = {
                        launchEffect {
                            val location = model.submitSuspend()
                            if (location != null) {
                                portal.go(LocationRoute(location.slug))
                            }
                        }
                    },
                    messages = model.message,
                    back = LabeledAction("go back", portal::goBack)
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

fun ViewScope.viewUpdateLocationRoute() {
    column {
        starGate { star ->
            // td: fix
            // routeBlock<LocationUpdateRoute, LocationUpdaterContent?>(
            //     portal = portal,
            //     provideData = { route ->
            //         api.readLocationUpdaterContent(route.slug).handleResponse(toaster)
            //     }
            // ) { content ->
            //     if (content == null) {
            //         textBlock("something went wrong")
            //         return@routeBlock
            //     }
//
            //     viewLocationUpdater(content, star)
            // }
        }
        appFooter("")
    }
}