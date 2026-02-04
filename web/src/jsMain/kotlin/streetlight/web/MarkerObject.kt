package streetlight.web

import org.w3c.dom.HTMLElement

data class MarkerObject(
    val marker: maplibregl.Marker,
    val entity: MarkerEntity,
    val element: HTMLElement?,
    val iconElement: HTMLElement?,
    val bearingElement: HTMLElement? = null,
) {
    var lastBearing = 0f

    fun setBearing(bearing: Float) {
        val be = bearingElement ?: return
        val delta = ((bearing - lastBearing + 540) % 360) - 180;
        lastBearing += delta
        val adjusted = lastBearing - 90
        be.style.setProperty("--bearing", "${adjusted}deg")
    }

    fun setOpacity(opacity: Float) {
        marker.setOpacity(opacity.toString())
    }
}