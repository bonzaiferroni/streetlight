package streetlight.web

import kampfire.model.GeoPoint
import koala.css.Css
import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.column
import koala.html.blockLabel

const val STOP_ZOOM = 14

fun RenderContext.viewStreetMap(
    app: AppContext,
) {
    column(modify(Width100)) {
        viewGeoMap(app.geoMap)
        viewMapPanel(app)
    }
}
