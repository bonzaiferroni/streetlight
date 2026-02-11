package streetlight.web

import koala.css.Height100
import koala.css.Height32
import koala.css.Height48
import koala.css.ModifierSet
import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.box
import koala.html.Id
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.dom.append
import org.w3c.dom.HTMLDivElement
import kotlinx.html.style
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

fun RenderContext.viewGeoMap(
    geoMap: GeoMap,
    modifiers: ModifierSet? = modify(Height48),
    block: (DIV.() -> Unit)? = null
): HTMLDivElement {
    val parent = box(GeoMapIds.mapMount, modify(Width100, modifiers)) {
        block?.invoke(this)
    }

    if (mapWindowElement == null) {
        mapWindowElement = createMapWindow(geoMap, parent)
    }

    onLoad {
        if (parent.children.length == 0) {
            console.log("grabbing geomap window")
            val mapWindow = mapWindowElement ?: error("mapWindowElement not found")
            parent.appendChild(mapWindow)
            mapWidget?.resize()
        }
    }

    return parent
}

var mapWindowElement: HTMLElement? = null
var mapWidget: maplibregl.Map? = null

fun RenderContext.createMapWindow(
    geoMap: GeoMap,
    parent: HTMLDivElement,
): HTMLElement {
    console.log("creating geomap")

    val mapWindow = parent.append {
        box(GeoMapIds.window, modify(Width100, Height100))
    }.first()

    val widgetBox = mapWindow.append {
        box(GeoMapIds.widget) {
        }
        box(GeoMapIds.overlay) {
            box(GeoMapIds.crosshairs)
        }
    }.first()

    renderScope.launch {

        val widget = maplibregl.Map(jsObject {
            container = widgetBox
            style = "https://tiles.openfreemap.org/styles/fiord"
            center = maplibregl.LngLat(-104.95, 39.75)
            zoom = 11
        })
        mapWidget = widget

        widget.addControl(maplibregl.NavigationControl())
        widget.addControl(maplibregl.FullscreenControl())

        fun relayBounds(isMoving: Boolean) {
            val bounds = widget.getBounds().toGeoBounds()
            val zoom = widget.getZoom()
            geoMap.setBounds(bounds, zoom.toFloat(), isMoving)
        }

        val markers = mutableMapOf<MapEntityId, MapObject>()

        val context = MapContext(widget)

        while (!widget.loaded()) {
            delay(10)
        }

        launch {
            console.log("collecting entities")
            geoMap.entityFlow.collect(context::collectEntity)
        }

        launch {
            geoMap.removeEntity.collect { entityId ->
                markers[entityId]?.marker?.remove()
                markers.remove(entityId)
            }
        }

        launch {
            geoMap.zoomFlow.collect { zoom ->
                markers.forEach { (_, obj) ->
                    val minZoom = obj.entity.minZoom ?: return@forEach
                    obj.setOpacity(if (zoom >= minZoom) 1f else 0f)
                }
            }
        }

        launch {
            geoMap.linesFlow.collect(context::showLines)
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

object GeoMapIds {
    val mapMount = Id("map-mount")
    val window = Id("map-window")
    val widget = Id("map-widget")
    val overlay = Id("map-overlay")
    val crosshairs = Id("map-crosshairs")
    val panel = Id("map-panel")
}