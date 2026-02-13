package streetlight.web.pages

import kotlinx.html.HEAD
import kotlinx.html.link
import kotlinx.html.script

fun HEAD.supportGeoMap() {
    link(href = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.css", rel = "stylesheet")
    script(src = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.js") { }
}

fun HEAD.supportProtobuf() {
    script(src = "https://cdn.jsdelivr.net/npm/protobufjs/dist/protobuf.min.js") { }
}