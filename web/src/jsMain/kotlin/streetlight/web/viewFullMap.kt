package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.Id
import kotlinx.browser.document
import streetlight.web.pages.SinglePageId

fun RenderContext.viewFullMap(app: AppContext) {
    mountFullscreen(Id("fullscreen-map")) {
        viewGeoMap(app.home.geoMap, "100%")
    }
}