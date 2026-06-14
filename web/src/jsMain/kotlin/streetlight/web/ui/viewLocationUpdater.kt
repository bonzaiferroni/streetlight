package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.LocationEdit
import streetlight.model.data.toEdit
import streetlight.web.LocationRoute
import streetlight.web.UpdateLocationRoute
import streetlight.web.model.LocationEditor
import streetlight.web.model.LocationScoutStage
import streetlight.web.ui.appFooter

fun AppScope.viewLocationUpdater(model: LocationEditor) {
    column {
        locationEditFormBody(model)
        formSubmit(
            label = "Next",
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

fun AppScope.viewUpdateLocationRoute() {
    column {
        routeBlock<UpdateLocationRoute, LocationEdit?>(
            portal = portal,
            provideData = { route ->
                api.readLocation(route.slug).handleResponse(toaster::toast)?.toEdit()
            }
        ) { edit ->
            val editor = edit?.let { app.getLocationEditor(it, parentScope) }
            when (editor) {
                null -> textBlock("something went wrong")
                else -> viewLocationUpdater(editor)
            }
        }
        appFooter("")
    }
}