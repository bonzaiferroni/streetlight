package streetlight.web

import koala.css.*
import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.web.shells.EventProfileShell
import streetlight.web.shells.eventProfileShell

fun RenderContext.viewEventRoute(
    app: AppContext,
) {
    val model = app.eventProfile

    flowBlock(model.eventFlow) {
        shellBox(EventProfileShell.id, modify(Width100)) {
            eventProfileShell(it)
        }
    }
    button("go home", onClickEvent = {
        app.portal.go(HomeRoute())
    })

    renderScope.launch {
        app.portal.routeFlowOf<EventRoute>().collect { route ->
            when (route) {
                is EventIdRoute -> {
                    model.fetchEvent(route.id)
                }

                is EventObjectRoute -> {
                    model.setEvent(route.event)
                }
            }
        }
    }
}