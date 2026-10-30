package streetlight.web.view

import koala.dom.*
import koala.model.GeoCamera
import koala.model.MarkerMap
import streetlight.web.model.geoMap
import streetlight.web.model.setPoints
import streetlight.model.data.EventLocation
import streetlight.model.ui.EventRoute
import streetlight.web.shell.EventShell
import streetlight.web.shell.eventShell
import koala.dom.page.*

fun ViewScope.viewEvent(event: EventLocation) {
    val camera = app.get<GeoCamera>()
    // val cache = app.get<DataCache>()

    val markerMap = MarkerMap(geoMap).apply { setPoints(listOf(event)) }
    shellBoxWithMap(markerMap) {
        eventShell(event)
    }

    camera.panMap(event.geoPoint)
}

fun RouteScope.viewEventRoute() {
    routeBlock<EventRoute, EventLocation>(EventShell.island) { event ->
        viewEvent(event)
    }
}