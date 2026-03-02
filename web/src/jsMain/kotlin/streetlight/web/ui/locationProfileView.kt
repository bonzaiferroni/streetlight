package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.model.data.Location
import streetlight.web.LocationAdminRoute
import streetlight.web.LocationProfileRoute
import streetlight.web.shells.LocationShell
import streetlight.web.shells.locationShell

fun RenderContext.locationProfileView(app: AppContext) {
    routeBlock<LocationProfileRoute, Location>(
        portal = app.portal,
        provideData = { app.client.api.readLocation(it.locationId) }
    ) { location ->
        val element = shellBox(LocationShell.id, app.geoMap, app.appScope) {
            locationShell(location)
        }

        wireBlock(LocationShell.adminCard, element) {
            userContent(app) { userInfo ->
                card {
                    row {
                        textBlock("hello ${userInfo.username}", modify(Flex1))
                        button("Admin panel", onClick = {
                            app.portal.go(LocationAdminRoute(location.locationId))
                        })
                    }
                }
            }
        }
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