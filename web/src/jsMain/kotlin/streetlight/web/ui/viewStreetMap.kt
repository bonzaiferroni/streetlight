package streetlight.web.ui

import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.column

fun RenderContext.viewStreetMap(
    app: AppContext,
) {
    column(modify(Width100)) {
        // wireGeoMap(app.geoMap)
        // viewMapPanel(app)
    }
}
