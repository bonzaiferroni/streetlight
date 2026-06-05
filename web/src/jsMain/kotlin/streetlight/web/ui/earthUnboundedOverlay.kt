package streetlight.web.ui

import kampfire.model.Point
import koala.css.*
import koala.dom.*
import koala.model.GeoMap
import koala.model.MapViewContext
import koala.model.MarkerId
import koala.model.toLngLat
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import streetlight.web.model.AppMarker
import streetlight.web.model.EarthMap
import kotlin.collections.first
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min

fun RenderContext.earthUnboundedOverlay(model: EarthMap, mapContext: MapViewContext) {
    val geoMap = app.get<GeoMap>()
    val widget = mapContext.widget
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

    fun createHint(marker: AppMarker) = element.append {
        image(marker.thumbUrl, modify(BorderRadius50P, Height5, Aspect1, PointerEventsAuto)).onClick {
            model.setFocus(marker)
        }
    }.first().let { MarkerHint(marker, it) }

    fun setState(hint: MarkerHint, offset: Point) {
        val geo = hint.marker.geoPoint
        val projected = widget.project(geo.toLngLat())

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

        val angle = atan2(dy, dx)

        hint.element.style.left = "${edgeX}px"
        hint.element.style.top = "${edgeY}px"
        hint.element.style.transform = "translate(-50%, -50%) rotate(${angle}rad)"
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
    val marker: AppMarker,
    val element: HTMLElement,
)