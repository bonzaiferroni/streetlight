package streetlight.web

import kampfire.model.GeoPoint

const val STOP_ZOOM = 14

fun RenderContext.viewMapWindow(
    maplibre: maplibregl.Map
) {
    val eventMap = app.home.eventMap

    maplibre.on("move") {
        val point = maplibre.getCenter().let { GeoPoint(it.lng, it.lat) }
        eventMap.setLocation(point)
    }

    maplibre.on("zoomend") {
        val zoom = maplibre.getZoom()
        console.log("zoom: $zoom")
        eventMap.setZoom(zoom.toFloat())
    }
}