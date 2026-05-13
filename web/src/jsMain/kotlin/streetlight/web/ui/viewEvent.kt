package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.GeoMap
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.EventLocation
import streetlight.web.EventObjectRoute
import streetlight.web.EventRoute
import streetlight.web.EventSlugRoute
import streetlight.web.io.ApiClient
import streetlight.web.model.DataCache
import streetlight.web.model.Streetlight
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
    // val model = app.eventProfile

    suspend fun provideData(route: EventRoute) = when (route) {
        is EventObjectRoute -> {
            route.event
        }
        is EventSlugRoute -> {
            api.readEventLocationBySlug(route.slug)
        }
    }

    routeBlock(portal, ::provideData) { event ->
        viewEvent(event)
    }
}