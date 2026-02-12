package streetlight.web

import koala.css.*
import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.web.shells.eventProfileShell

fun RenderContext.viewEventRoute(
    app: AppContext,
) {
    val model = app.eventProfile

    flowBlock(model.eventFlow) {
        shellBox(modify(Width100)) {
            eventProfileShell(it)
        }
    }
    button("go home", onClick = {
        app.portal.go(HomeRoute())
    })

    renderScope.launch {
        app.portal.routeFlowOf<EventRoute>().collect { route ->
            when (route) {
                is EventIdRoute -> {
                    model.fetchEvent(route.eventId)
                }

                is EventObjectRoute -> {
                    model.setEvent(route.event)
                }
            }
        }
    }
}