package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import streetlight.model.data.LocationEdit
import streetlight.model.data.toEdit
import streetlight.web.CreateLocationRoute
import streetlight.web.EditLocationDataRoute
import streetlight.web.EditLocationIdRoute
import streetlight.web.EditLocationRoute
import streetlight.web.pages.appFooter

fun RenderContext.viewEditLocationRoute() {
    column {
        routeBlock<EditLocationRoute, LocationEdit>(
            portal = portal,
            provideData = { route ->
                when (route) {
                    is EditLocationDataRoute -> route.location
                    is EditLocationIdRoute -> route.locationId?.let {
                        api.readLocation(it).handleResponse(toaster::toast)?.toEdit()
                    } ?: LocationEdit()
                    is CreateLocationRoute -> LocationEdit()
                }
            }
        ) { edit ->
            // val editor = LocationEditor(edit, renderScope, api, Toaster(renderScope))
            // viewLocationEditorProto(editor)
        }
        appFooter()
    }
}