package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.model.GeoMap
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.EventLocation
import streetlight.web.EventRoute
import streetlight.web.model.DataCache
import streetlight.web.shells.EventProfileKey
import streetlight.web.shells.eventShell

fun RenderContext.viewEvent(event: EventLocation) {
    val geoMap = app.get<GeoMap>()
    val appScope = app.get<CoroutineScope>()
    val cache = app.get<DataCache>()

    val root = shellBox(EventProfileKey.id, geoMap, appScope, modify(Width100P)) {
        eventShell(event)
    }

    geoMap.panMap(event.geoPoint)
    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = cache.eventLights
    )
    // app.streetMap.setPosts td: make event marker visible on map
}

fun RenderContext.viewEventProfileRoute() {

    routeBlock<EventRoute, EventLocation>(portal, { route ->
        api.readEventSlug(route.slug).handleResponse(toaster::toast)
    }) { event ->
        viewEvent(event)
    }
}