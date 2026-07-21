package koala.model

import koala.dom.launch
import koala.dom.modify
import koala.dom.onView
import koala.dom.unmodify
import koala.external.CenterZoomBearing
import koala.external.CenterZoomBearing.Companion.invoke
import koala.external.maplibregl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement

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
                delay(10)
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

            launch("collect pan bounds") {
                camera.panBoundsFlow.collect {
                    jsMap.fitBounds(it.toLngLatBounds())
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

    fun setAltitude(zoom: Double) {
        val altitude = altitudeOf(zoom)
        if (altitude == altitudeNow) return

        Altitude.entries.forEach {
            when (zoom < it.zoom) {
                true -> windowElement.modify(it)
                else -> windowElement.unmodify(it)
            }
        }
        
        altitudeNow = altitude
    }

    fun relayBounds(isMoving: Boolean) {
        val center = jsMap.getCenter().toGeoPoint()
        val bounds = jsMap.getBounds().toGeoBounds()
        val zoom = jsMap.getZoom().toFloat()
        camera.setBounds(center, bounds, zoom, isMoving)
    }
}
