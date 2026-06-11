package streetlight.web.ui

import kampfire.model.Point
import koala.css.*
import koala.dom.*
import koala.external.maplibregl
import koala.model.GeoCamera
import koala.model.GeoCameraController
import koala.model.MarkerId
import koala.model.toLngLat
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import streetlight.web.model.FeatureMarker
import streetlight.web.model.EarthMap
import kotlin.collections.first
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

fun RenderContext.earthUnboundedOverlay(model: EarthMap, mapContext: GeoCameraController) {
    val geoMap = app.get<GeoCamera>()
    val widget = mapContext.jsMap
    val element = div(modify(Earth.Unbounded, PointerEventsNone))
    val offsetFlow = element.resizeFlow()
        .combine(mapContext.windowElement.resizeFlow()) { _, _ -> Unit }
        .map {
            val overlayRect = element.getBoundingClientRect()
            val mapRect = mapContext.windowElement.getBoundingClientRect()
            Point(overlayRect.left - mapRect.left, overlayRect.top - mapRect.top)
        }
        .distinctUntilChanged()

    val hints = mutableMapOf<MarkerId, MarkerHint>()

    fun createHint(marker: FeatureMarker) = element.append {
        image(marker.thumbUrl, modify(BorderRadius50P, Height5, Aspect1, PointerEventsAuto, StartingOpacity0)).onClick {
            model.setFocus(marker)
        }
    }.first().let { MarkerHint(marker, it) }

    fun setState(hint: MarkerHint, offset: Point) {
        val geo = hint.marker.geoPoint
        val center = widget.getCenter()

        // clamp the target near the viewport so it can't fall behind the camera
        val bounds = widget.getBounds()
        val maxSpan = min(
            bounds.getEast() - bounds.getWest(),
            bounds.getNorth() - bounds.getSouth(),
        )
        var dLng = geo.lng - center.lng
        var dLat = geo.lat - center.lat
        val span = max(abs(dLng), abs(dLat))
        if (span > maxSpan) {
            val scale = maxSpan / span
            dLng *= scale
            dLat *= scale
        }
        val projected = widget.project(maplibregl.LngLat(center.lng + dLng, center.lat + dLat))

        // marker position in overlay coords
        val mx = projected.x - offset.x
        val my = projected.y - offset.y

        val halfW = element.clientWidth / 2.0
        val halfH = element.clientHeight / 2.0
        val dx = mx - halfW
        val dy = my - halfH

        // ray-rect intersection, inset so the hint isn't clipped at the rim
        val padding = 8.0
        val t = min(
            (halfW - padding) / abs(dx),
            (halfH - padding) / abs(dy),
        )
        val edgeX = halfW + dx * t
        val edgeY = halfH + dy * t

        // val angle = atan2(dy, dx)

        hint.element.style.left = "${edgeX}px"
        hint.element.style.top = "${edgeY}px"
        hint.element.style.transform = "translate(-50%, -50%)"
    }

    launchRender {
        launch {
            combine(
                model.unboundedMarkersFlow,
                offsetFlow,
                geoMap.movingBoundsFlow,
            ) { markers, offset, _ -> markers to offset }
                .collect { (markers, offset) ->
                    hints.entries.removeAll { (id, hint) ->
                        if (markers.none { it.markerId == id }) {
                            hint.element.remove()
                            true
                        } else false
                    }
                    markers.forEach { marker ->
                        val hint = hints.getOrPut(marker.markerId) { createHint(marker) }
                        setState(hint, offset)
                    }
                }
        }
    }
}

private data class MarkerHint(
    val marker: FeatureMarker,
    val element: HTMLElement,
)