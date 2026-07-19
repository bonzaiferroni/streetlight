package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import koala.model.GeoCamera
import streetlight.model.data.EventLocation
import streetlight.model.ui.EventRoute
import streetlight.web.model.DataCache
import streetlight.web.shells.EventShell
import streetlight.web.shells.eventShell

fun ViewScope.viewEvent(event: EventLocation) {
    val camera = app.get<GeoCamera>()
    // val cache = app.get<DataCache>()

    val root = shellBoxWithMap(EventShell.id, hookInitializers) {
        eventShell(event)
    }

    camera.panMap(event.geoPoint)
    // wireLights(
    //     root = root,
    //     attribute = StarLightKey.EventLightId,
    //     cache = cache.eventLights
    // )
    // app.streetMap.setPosts td: make event marker visible on map
}

fun RouteScope.viewEventRoute() {
    routeBlock<EventRoute, EventLocation>(EventShell.island) { event ->
        viewEvent(event)
    }
}