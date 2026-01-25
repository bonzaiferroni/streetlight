package streetlight.web

import kotlinx.browser.window

fun maplibregl.LngLat.interpolateTo(dest: maplibregl.LngLat, t: Double): maplibregl.LngLat =
    maplibregl.LngLat(
        lng = lng + (dest.lng - lng) * t,
        lat = lat + (dest.lat - lat) * t
    )

fun maplibregl.Marker.move(
    origin: maplibregl.LngLat,
    destination: maplibregl.LngLat,
    durationMs: Double = 1000.0,
) {
    var start: Double? = null

    fun tick(time: Double) {
        if (start == null) start = time
        val t = ((time - start) / durationMs).coerceIn(0.0, 1.0)
        setLngLat(origin.interpolateTo(destination, t))
        if (t < 1.0) window.requestAnimationFrame(::tick)
    }

    window.requestAnimationFrame(::tick)
}

fun Position.toLngLat() = maplibregl.LngLat(longitude.toDouble(), latitude.toDouble())