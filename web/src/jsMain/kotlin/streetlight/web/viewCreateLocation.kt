package streetlight.web

import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.row
import koala.html.paragraph

fun RenderContext.viewCreateLocation(
    eventMap: EventMap,
    portal: AppPortal,
) {
    column {
        renderState(eventMap.newLocationFlow) { location ->
            row {
                paragraph("Location: ${location.name}")
            }
        }
        row {
            button("cancel") {
                portal.goBack()
            }
            button("query") {
                eventMap.queryLocation()
            }
            button("create") {
                eventMap.createLocation()
                portal.goBack()
            }
        }
    }
}