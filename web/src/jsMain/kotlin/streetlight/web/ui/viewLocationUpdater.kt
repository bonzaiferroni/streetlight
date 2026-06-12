package streetlight.web.ui

import koala.dom.*
import streetlight.web.pages.appFooter

fun RenderScope.viewEditLocationRoute() {
    column {
//        routeBlock<UpdateLocationRoute, LocationEdit>(
//            portal = portal,
//            provideData = { route ->
//                when (route) {
//                    is EditLocationDataRoute -> route.location
//                    is EditLocationIdRoute -> route.locationId?.let {
//                        api.readLocation(it).handleResponse(toaster::toast)?.toEdit()
//                    } ?: LocationEdit()
//                    is CreateLocationRoute -> LocationEdit()
//                }
//            }
//        ) { edit ->
//            // val editor = LocationEditor(edit, renderScope, api, Toaster(renderScope))
//            // viewLocationEditorProto(editor)
//        }
        appFooter()
    }
}