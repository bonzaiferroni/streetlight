package streetlight.web

import koala.html.*
import kotlinx.browser.window
import kotlinx.html.js.div
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.html.visitAndFinalize

fun AppContext.viewHome() {
    val maplibre = maplibregl.Map(jsObject {
        container = "geo-map"
        style = "https://tiles.openfreemap.org/styles/fiord"
        center = maplibregl.LngLat(-104.95, 39.75)
        zoom = 11
    })

    viewMapWindow(maplibre)
    viewGtfsMap(maplibre)
    viewMapPanel()
}