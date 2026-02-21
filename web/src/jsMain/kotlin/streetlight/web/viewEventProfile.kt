package streetlight.web

import koala.css.*
import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.web.shells.EventProfileShell
import streetlight.web.shells.eventProfileShell

fun RenderContext.viewEventRoute(
    app: AppContext,
) {
    // val model = app.eventProfile
    val api = app.client.api
    val portal = app.portal

    suspend fun provideData(route: EventRoute) = when (route) {
        is EventIdRoute -> {
            api.readEvent(route.id)
        }
        is EventObjectRoute -> {
            route.event
        }
    }

    column {
        routeBlock(portal, ::provideData) {
            shellBox(EventProfileShell.id, modify(Width100)) {
                eventProfileShell(it)
            }
        }

        button("go home", onClickEvent = {
            app.portal.go(HomeRoute())
        })
    }
}