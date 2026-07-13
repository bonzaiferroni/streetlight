package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.css.*
import koala.dom.*
import koala.model.GeoCamera
import streetlight.model.data.EventLocation
import streetlight.web.EventRoute
import streetlight.web.model.DataCache
import streetlight.web.shells.EventShell
import streetlight.web.shells.eventShell

fun AppScope.viewEvent(event: EventLocation) {
    val camera = app.get<GeoCamera>()
    val cache = app.get<DataCache>()

    val root = shellBoxWithMap(EventShell.id, hookInitializers) {
        eventShell(event)
    }

    camera.panMap(event.geoPoint)
    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = cache.eventLights
    )
    // app.streetMap.setPosts td: make event marker visible on map
}

fun AppScope.viewEventProfileRoute() {

    routeBlock<EventRoute, EventLocation>(portal, { route ->
        readIsland<EventLocation>(EventShell.island) { it.eventSlug == route.slug }
            ?: api.readEventSlug(route.slug).handleOutcome(toaster::toast)
    }) { event ->
        viewEvent(event)
    }
}