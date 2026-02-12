package streetlight.web

import koala.dom.RenderContext
import koala.dom.button
import koala.dom.flowBlock
import koala.dom.textBlock
import kotlinx.coroutines.flow.filterNotNull

fun RenderContext.viewEvent(
    portal: AppPortal,
) {
    flowBlock(portal.routeFlow.mapDistinct { it as? EventRoute }.filterNotNull()) { route ->
        textBlock(route.id.value)
    }

    button("go home", onClick = {
        portal.go(HomeRoute())
    })
}