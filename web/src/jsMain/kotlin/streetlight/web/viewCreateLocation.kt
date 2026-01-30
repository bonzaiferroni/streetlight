package streetlight.web

import kampfire.model.GeoPoint
import koala.dom.button
import koala.dom.column
import koala.dom.row
import koala.html.paragraph
import streetlight.model.data.AreaId
import streetlight.model.data.Location
import streetlight.model.data.NewLocation

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