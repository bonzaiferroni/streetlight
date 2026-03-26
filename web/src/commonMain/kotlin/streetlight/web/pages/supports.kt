package streetlight.web.pages

import kotlinx.html.HEAD
import kotlinx.html.link
import kotlinx.html.onLoad
import kotlinx.html.script

fun HEAD.supportGeoMap() {
    link(
        href = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.css",
        rel = "stylesheet",
    ) {
        media = "print"
        onLoad = "this.media='all'"
    }
    script(src = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.js") {
        defer = true
    }
}

fun HEAD.supportProtobuf() {
    script(src = "https://cdn.jsdelivr.net/npm/protobufjs/dist/protobuf.min.js") {
        defer = true
    }
}