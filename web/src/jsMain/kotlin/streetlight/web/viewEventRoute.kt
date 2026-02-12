package streetlight.web

import koala.dom.RenderContext
import koala.dom.button
import koala.dom.flowBlock
import koala.dom.textBlock
import koala.model.Portal
import kotlinx.coroutines.launch

fun RenderContext.viewEventFromId(app: AppContext) {

}

fun RenderContext.viewEventRoute(
    portal: Portal,
) {
    flowBlock(portal.routeFlowOf<EventRoute>()) { route ->
        when (route) {
            is EventIdRoute -> {
                textBlock(route.eventId.value)
            }
            is EventObjectRoute -> TODO()
        }
    }

    button("go home", onClick = {
        portal.go(HomeRoute())
    })
}