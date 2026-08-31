package streetlight.web.ui

import kampfire.model.Point
import koala.css.*
import koala.dom.*
import koala.external.maplibregl
import koala.model.FeatureMarker
import koala.model.GeoCamera
import koala.model.GeoCameraController
import koala.model.IconMarker
import koala.model.MarkerId
import koala.model.ThumbMarker
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import streetlight.web.model.Earth
import web.html.HTMLElement
import kotlin.collections.first
import kotlin.math.min

fun ViewScope.earthUnboundedOverlay(model: Earth, mapContext: GeoCameraController) {
    val geoMap = app.get<GeoCamera>()
    val widget = mapContext.jsMap
    val element = div(modify(EarthStyle.Unbounded, PointerEventsNone))
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
        when (marker) {
            is ThumbMarker -> image(marker.thumbUrl, modify(
                BorderRadius50P, Height5, Aspect1, PointerEventsAuto, FadeIn, OpacityHalf
            )).onClick {
                model.setFocus(marker)
            }
            is IconMarker -> icon(marker.svg, modify(Height3, OpacityHalf, PointerEventsAuto, ColorSchemeFg)) {
                marker.colorScheme?.let {
                    setStyle(Property.ColorScheme.to(it))
                }
            }.onClick {
                model.setFocus(marker)
            }
        }
    }.first().let { MarkerHint(marker, it) }

    fun setState(hint: MarkerHint, offset: Point) {
        val geo = hint.marker.geoPoint
        val center = widget.getCenter()

        val bounds = widget.getBounds()
        val maxSpan = min(
            bounds.getEast() - bounds.getWest(),
            bounds.getNorth() - bounds.getSouth(),
        )
        val dLng = (geo.lng - center.lng).coerceIn(-maxSpan, maxSpan)
        val dLat = (geo.lat - center.lat).coerceIn(-maxSpan, maxSpan)
        val projected = widget.project(maplibregl.LngLat(center.lng + dLng, center.lat + dLat))

        val mx = projected.x - offset.x
        val my = projected.y - offset.y

        val padding = 8.0
        val edgeX = mx.coerceIn(padding, element.clientWidth - padding)
        val edgeY = my.coerceIn(padding, element.clientHeight - padding)

        // val angle = atan2(my - edgeY, mx - edgeX)

        hint.element.style.left = "${edgeX}px"
        hint.element.style.top = "${edgeY}px"
        hint.element.style.transform = "translate(-50%, -50%)" // rotate(${angle}rad)
    }

    launchEffect(ViewScope::earthUnboundedOverlay) {
        launch {
            combine(
                model.unboundedMarkersField.flow,
                offsetFlow,
                geoMap.movingBoundsField.flow,
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