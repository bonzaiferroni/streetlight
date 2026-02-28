package koala.dom

import koala.core.findAndInitGeoMap
import koala.core.queryFirstOrNull
import koala.css.Blur
import koala.css.SlideX
import koala.css.modify
import koala.external.CenterZoomBearing
import koala.external.maplibregl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import koala.html.GeoMapSelector
import koala.html.cardOf
import koala.model.GeoMap
import koala.model.MapViewContext
import koala.model.PointEntity
import koala.model.mapDistinct
import koala.model.showLines
import koala.model.toGeoBounds
import koala.model.toGeoPoint
import koala.model.toLngLat
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.html.FlowContent

fun wireGeoMap(
    geoMap: GeoMap,
    appScope: CoroutineScope,
    ancestor: HTMLElement,
) {
    val mount = ancestor.queryFirstOrNull(GeoMapSelector.mapMount) ?: return
    val mapWindow = wireMapWindow(geoMap, appScope, mount)

    mount.onView { isVisible ->
        if (isVisible && mount.children.length == 0) {
            console.log("grabbing geomap window")
            mount.appendChild(mapWindow)
            // mapWidget?.resize()
        }
    }
}

private var geoMapWindow: HTMLElement? = null

fun wireMapWindow(
    geoMap: GeoMap,
    appScope: CoroutineScope,
    mount: HTMLElement
): HTMLElement {
    geoMapWindow?.let {
        return it
    }

    console.log("creating geomap")
    val mapWindow = document.getElementOrNullById(GeoMapSelector.window) ?: findAndInitGeoMap(mount) ?: error("geomap window not found")
    geoMapWindow = mapWindow
    val widget: maplibregl.Map = mapWindow.asDynamic().widget ?: error("geomap widget not found")

    mapWindow.onView(geoMap::setIsViewed)

    val focusPanel = mapWindow.querySelector(GeoMapSelector.focusPanel.selector) as HTMLElement
    focusPanel.renderRoot(appScope) {
        val nearestFlow = geoMap.stateFlow.mapDistinct { it.nearest }
        flowBlock(nearestFlow, modify(Blur, SlideX), magic = true) { entity ->
            val cardFunction = entity?.focusCard ?: return@flowBlock
            cardFunction()
        }
    }

    appScope.launch {

        val context = MapViewContext(widget)

        fun relayBounds(isMoving: Boolean) {
            val bounds = widget.getBounds().toGeoBounds()
            val zoom = widget.getZoom().toFloat()
            val nearest = context.getNearest(widget.getCenter().toGeoPoint(), zoom)
            geoMap.setBounds(bounds, zoom, isMoving, nearest)
        }

        while (!widget.loaded()) {
            delay(10)
        }

        launch {
            geoMap.entityFlow.collect(context::addEntities)
        }

        launch {
            geoMap.removeEntity.collect { entityIds ->
                entityIds.forEach { entityId ->
                    context.markers[entityId]?.marker?.remove()
                    context.markers.remove(entityId)
                }
            }
        }

        launch {
            geoMap.zoomFlow.collect { zoom ->
                context.markers.forEach { (_, obj) ->
                    val minZoom = obj.entity.minZoom ?: return@forEach
                    obj.setOpacity(if (zoom >= minZoom) 1f else 0f)
                }
            }
        }

        launch {
            geoMap.linesFlow.collect(context::showLines)
        }

        launch {
            geoMap.panFlow.collect { pan ->
                console.log("panning to: ${pan.point.toLngLat()}")
                val options = CenterZoomBearing(
                    center = pan.point.toLngLat(),
                    zoom = pan.zoom?.toDouble(),
                )
                if (pan.snap) {
                    widget.jumpTo(options)
                } else if (pan.zoom != null) {
                    widget.flyTo(options)
                } else {
                    widget.panTo(pan.point.toLngLat())
                }
            }
        }

        launch {
            geoMap.markerVisibilityFlow.collect { provideVisibility ->
                context.setVisibility(provideVisibility)
            }
        }

        widget.on("move") {
            relayBounds(true)
        }

        widget.on("moveend") {
            relayBounds(false)
        }

        relayBounds(false)
    }

    return mapWindow
}