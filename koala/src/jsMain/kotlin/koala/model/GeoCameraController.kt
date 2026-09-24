package koala.model

import koala.utils.launch
import koala.modifier.modify
import koala.dom.onView
import koala.modifier.unmodify
import koala.external.CenterZoomBearing
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import web.html.HTMLElement
import web.timers.setTimeout
import kotlin.time.Duration.Companion.milliseconds

/** Connects the MapLibre map to its [camera]: carries out pans and reports the map's position and altitude back. */
class GeoCameraController(
    val jsMap: maplibregl.Map,
    val windowElement: HTMLElement,
    val camera: GeoCamera,
    val scope: CoroutineScope,
) {
    private var altitudeNow: Altitude? = null
    // private var boundsNow: GeoBounds? = null

    init {
        windowElement.onView(camera::setIsViewed)

        scope.launch(GeoCameraController::class) {
            while (!jsMap.loaded()) {
                delay(10.milliseconds)
            }

            launch("collect pan") {
                camera.panFlow.collect { pan ->
                    console.log("panning to: ${pan.point.toLngLat()}")
                    val options = CenterZoomBearing(
                        center = pan.point.toLngLat(),
                        zoom = pan.zoom?.toDouble(),
                    )
                    if (pan.snap) {
                        jsMap.jumpTo(options)
                    } else if (pan.zoom != null) {
                        jsMap.flyTo(options)
                    } else {
                        jsMap.panTo(pan.point.toLngLat())
                    }
                }
            }

            var baseElement: HTMLElement? = null
            launch("collect pan bounds") {
                camera.panBoundsFlow.collect { bounds ->
                    // td: find better solution (adds delay if map window was moved)
                    val base = windowElement.parentElement?.parentElement
                    if (base == baseElement) {
                        jsMap.fitBounds(bounds.toLngLatBounds())
                        return@collect
                    }
                    setTimeout({
                        baseElement = base
                        jsMap.fitBounds(bounds.toLngLatBounds())
                    }, 100)
                }
            }

            setAltitude(jsMap.getZoom())
            jsMap.on("zoom") {
                setAltitude(jsMap.getZoom())
            }

            relayBounds(false)
            jsMap.on("move") {
                relayBounds(true)
            }

            jsMap.on("moveend") {
                relayBounds(false)
            }
        }
    }

    /** Sets the altitude classes of the map window for [zoom]. */
    fun setAltitude(zoom: Double) {
        val altitude = altitudeOf(zoom)
        if (altitude == altitudeNow) return

        Altitude.entries.forEach {
            when (zoom < it.zoom) {
                true -> windowElement.modify(it.cssClass)
                else -> windowElement.unmodify(it.cssClass)
            }
        }
        
        altitudeNow = altitude
    }

    /** Reports the map's center, bounds and zoom to the camera. */
    fun relayBounds(isMoving: Boolean) {
        val center = jsMap.getCenter().toGeoPoint()
        val bounds = jsMap.getBounds().toGeoBounds()
        val zoom = jsMap.getZoom().toFloat()
        camera.setBounds(center, bounds, zoom, isMoving)
    }
}
