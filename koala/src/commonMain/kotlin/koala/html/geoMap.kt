package koala.html

import kotlinx.html.*

fun FlowContent.geoMap(
    width: String = "100%",
    height: String = "400px",
) {
    column {
        style = "width: $width;"
        box(Id("geo-map-box")) {
            style = "width: $width; height: $height;"
            box(Id("geo-map"))
            box(Id("geo-overlay")) {
                box(Id("geo-crosshairs"))
            }
        }
        div {
            id = "map-panel"
        }
    }
}

fun HEAD.geoMapResources() {
    script(src = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.js") { }
    link(href = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.css", "stylesheet")
}