package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.model.data.Location
import streetlight.web.LocationAdminRoute
import streetlight.web.LocationIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.LocationProfileKey
import streetlight.web.shells.locationProfileShell

fun RenderContext.locationProfileView(app: Streetlight) {
    routeBlock<LocationIdRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) { location ->
        val element = shellBox(LocationProfileKey.Id, app.geoMap, app.appScope) {
            locationProfileShell(location)
        }

//        wireBlock(LocationProfileKey.adminCard, element) {
//            starBlock(app) { userInfo ->
//                card {
//                    row {
//                        textBlock("hello ${userInfo.username}", modify(Flex1))
//                        button("Admin panel", onClick = {
//                            app.portal.go(LocationAdminRoute(location.locationId))
//                        })
//                    }
//                }
//            }
//        }
    }
}

// location.link?.let {
//                button("check", onClick = {
//                    renderScope.launch {
//                        app.client.api.parseLocation(it)?.toLocationEdit(location.locationId)?.let { edit ->
//                            app.portal.go(EditLocationDataRoute(edit))
//                        }
//                    }
//                })
//            }