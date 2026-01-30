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
        paragraph("create location")
        row {
            button("cancel") {
                portal.goBack()
            }
            button("create") {
                home.eventMap.createLocation(NewLocation(
                    areaId = AreaId.random(),
                    name = "owltown",
                    geoPoint = GeoPoint.Denver
                ))
                portal.goBack()
            }
        }
    }
}