package streetlight.web

import kampfire.model.GeoPoint

const val STOP_ZOOM = 14

fun AppContext.viewMapWindow(
    maplibre: maplibregl.Map
) {
    val eventMap = home.eventMap

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