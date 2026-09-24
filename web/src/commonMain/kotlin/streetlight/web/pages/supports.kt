package streetlight.web.pages

import kotlinx.html.HEAD
import kotlinx.html.link
import kotlinx.html.onLoad
import kotlinx.html.script

/** Loads MapLibre, its stylesheet without blocking render. */
fun HEAD.supportGeoMap() {
    link(
        href = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.24.0/dist/maplibre-gl.css",
        rel = "stylesheet",
    ) {
        media = "print"
        onLoad = "this.media='all'"
    }
    script(src = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.24.0/dist/maplibre-gl.js") {
        defer = true
    }
}

/** Loads protobuf.js, which reads transit feeds. */
fun HEAD.supportProtobuf() {
    script(src = "https://cdn.jsdelivr.net/npm/protobufjs/dist/protobuf.min.js") {
        defer = true
    }
}