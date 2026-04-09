package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.EventObjectRoute
import streetlight.web.EventRoute
import streetlight.web.EventSlugRoute
import streetlight.web.HomeRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.EventProfileShell
import streetlight.web.shells.eventProfileShell

fun RenderContext.viewEventRoute(
    app: Streetlight,
) {
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

    column {
        routeBlock(portal, ::provideData) {
            shellBox(EventProfileShell.id, modify(Width100P)) {
                eventProfileShell(it)
            }
        }

        button("go home", onClickEvent = {
            app.portal.go(HomeRoute)
        })
    }
}