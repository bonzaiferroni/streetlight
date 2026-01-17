package streetlight.web

import org.w3c.dom.HTMLElement

data class Bus(
    val marker: maplibregl.Marker,
    val element: HTMLElement,
    val bearingElement: HTMLElement,
) {
    var lastBearing = 0f

    fun setBearing(bearing: Float) {
        val delta = ((bearing - lastBearing + 540) % 360) - 180;
        lastBearing += delta
        val adjusted = lastBearing - 90
        bearingElement.style.setProperty("--bearing", "${adjusted}deg")
    }
}