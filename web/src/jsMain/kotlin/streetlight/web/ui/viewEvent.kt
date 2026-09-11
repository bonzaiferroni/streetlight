package streetlight.web.ui

import koala.dom.*
import koala.model.GeoCamera
import streetlight.model.data.EventLocation
import streetlight.model.ui.EventRoute
import streetlight.web.shells.EventShell
import streetlight.web.shells.eventShell

fun ViewScope.viewEvent(event: EventLocation) {
    val camera = app.get<GeoCamera>()
    // val cache = app.get<DataCache>()

    val root = shellBoxWithMap(EventShell.id) {
        eventShell(event)
    }

    camera.panMap(event.geoPoint)
}

fun RouteScope.viewEventRoute() {
    routeBlock<EventRoute, EventLocation>(EventShell.island) { event ->
        viewEvent(event)
    }
}