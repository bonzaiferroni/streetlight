package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.model.data.EventLocation
import streetlight.web.EventObjectRoute
import streetlight.web.EventRoute
import streetlight.web.EventSlugRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.EventProfileShell
import streetlight.web.shells.eventProfileShell

fun ViewContext<Streetlight>.viewEventProfile(event: EventLocation) {
    val app = model

    val root = shellBox(EventProfileShell.id, app.geoMap, app.appScope, modify(Width100P)) {
        eventProfileShell(event)
    }

    app.geoMap.panMap(event.geoPoint)
    wireLights(
        root = root,
        attribute = StarLightKey.EventLightId,
        cache = app.cache.event
    )
    // app.streetmMp.setPosts td: make event marker visible on map
}

fun ViewContext<Streetlight>.viewEventProfileRoute() {
    val app = model
    // val model = app.eventProfile
    val api = app.client.api
    val portal = app.portal

    suspend fun provideData(route: EventRoute) = when (route) {
        is EventObjectRoute -> {
            route.event
        }
        is EventSlugRoute -> {
            api.readEventLocationBySlug(route.slug)
        }
    }

    routeBlock(portal, ::provideData) { event ->
        viewContextOf(app) {
            viewEventProfile(event)
        }
    }
}