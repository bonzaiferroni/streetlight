package koala.html

import koala.css.Width100
import koala.css.modify
import kotlinx.html.*

fun FlowContent.geoMap(
    width: String = "100%",
    height: String = "400px",
) {
    column {
        style = "width: $width;"
        box(MapId.window) {
            style = "width: $width; height: $height;"
            box(MapId.widget)
            box(MapId.overlay) {
                box(MapId.crosshairs)
            }
        }
        box(MapId.panel, modify(Width100))
    }
}

object MapId {
    val window = Id("map-window")
    val widget = Id("map-widget")
    val overlay = Id("map-overlay")
    val crosshairs = Id("map-crosshairs")
    val panel = Id("map-panel")
}

fun HEAD.geoMapResources() {
    script(src = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.js") { }
    link(href = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.css", "stylesheet")
}