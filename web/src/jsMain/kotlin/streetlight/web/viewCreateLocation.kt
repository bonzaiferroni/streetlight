package streetlight.web

import koala.dom.button
import koala.dom.column
import koala.dom.row
import koala.html.paragraph

fun RenderContext.viewCreateLocation() {
    column {
        renderState(home.eventMap.newLocationFlow) { location ->
            row {
                paragraph("Location: ${location.name}")
            }
        }
        row {
            button("cancel") {
                portal.goBack()
            }
            button("query") {
                home.eventMap.queryLocation()
            }
            button("create") {
                home.eventMap.createLocation()
                portal.goBack()
            }
        }
    }
}