package koala.dom

import koala.core.findAndInitGeoMap
import koala.core.queryFirstOrNull
import koala.external.CenterZoomBearing
import koala.external.maplibregl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import koala.html.GeoMapSelector
import koala.model.GeoMap
import koala.model.MapViewContext
import koala.model.mapDistinct
import koala.model.showLines
import koala.external.maplibregl.Point
import koala.model.toGeoBounds
import koala.model.toGeoPoint
import koala.model.toLngLat
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope

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
    // wireKeyboardControls(widget)

    val focusPanel = mapWindow.querySelector(GeoMapSelector.focusPanel.selector) as HTMLElement
    focusPanel.renderRoot(appScope) {
        val nearestFlow = geoMap.stateFlow.mapDistinct { it.nearest }
        flowBlock(nearestFlow, defaultMagic, magic = true) { entity ->
            val cardFunction = entity?.focusCard ?: return@flowBlock
            cardFunction()
        }
    }

    appScope.launch {

        val context = MapViewContext(widget, mapWindow)

        fun relayBounds(isMoving: Boolean) {
            val center = widget.getCenter().toGeoPoint()
            val bounds = widget.getBounds().toGeoBounds()
            val zoom = widget.getZoom().toFloat()
            val nearest = context.setBounds(bounds, center, zoom)
            geoMap.setBounds(center, bounds, zoom, isMoving, nearest)
        }

        while (!widget.loaded()) {
            delay(10)
        }

        launch {
            geoMap.entityFlow.collect(context::addEntities)
        }

        launch {
            geoMap.removeEntity.collect(context::removeEntities)
        }

        launch {
            geoMap.tempEntityFlow.collect(context::tempEntitySet)
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

        launch {
            geoMap.movementFlow.collect { movement ->
                context.moveEntity(movement)
            }
        }

        launch {
            geoMap.contextIdFlow.collect {
                context.setContextId(it)
            }
        }

        // relay zoom
        context.setAltitude(widget.getZoom())
        widget.on("zoom") {
            context.setAltitude(widget.getZoom())
        }

        // relay bounds
        relayBounds(false)
        widget.on("move") {
            relayBounds(true)
        }

        widget.on("moveend") {
            relayBounds(false)
        }
    }

    return mapWindow
}

fun wireKeyboardControls(widget: maplibregl.Map) {
    val pressedKeys = mutableMapOf<String, Boolean>()

    window.addEventListener("keydown", { event ->
        event as org.w3c.dom.events.KeyboardEvent
        if (event.altKey || event.ctrlKey || event.metaKey || event.shiftKey) return@addEventListener
        pressedKeys[event.code] = true
    }, false)

    window.addEventListener("keyup", { event ->
        event as org.w3c.dom.events.KeyboardEvent
        pressedKeys[event.code] = false
    }, false)

    // pixels the map pans per second
    val panSpeed = 500.0
    // degrees the map rotates per second
    val rotationSpeed = 100.0

    var lastTime = window.performance.now()

    fun frame(time: Double) {
        val dt = ((time - lastTime) / 1000.0).coerceAtMost(0.1)
        lastTime = time

        var dx = 0.0
        var dy = 0.0
        var dBearing = 0.0

        if (pressedKeys["KeyW"] == true || pressedKeys["ArrowUp"] == true) dy -= 1.0
        if (pressedKeys["KeyS"] == true || pressedKeys["ArrowDown"] == true) dy += 1.0
        if (pressedKeys["KeyA"] == true || pressedKeys["ArrowLeft"] == true) dx -= 1.0
        if (pressedKeys["KeyD"] == true || pressedKeys["ArrowRight"] == true) dx += 1.0
        if (pressedKeys["KeyQ"] == true) dBearing -= 1.0
        if (pressedKeys["KeyE"] == true) dBearing += 1.0

        if (dx != 0.0 && dy != 0.0) {
            val mag = kotlin.math.sqrt(dx * dx + dy * dy)
            dx /= mag
            dy /= mag
        }

        if (dx != 0.0 || dy != 0.0) {
            widget.panBy(Point(dx * panSpeed * dt, dy * panSpeed * dt), js("{animate: false}"))
        }

        if (dBearing != 0.0) {
            widget.setBearing(widget.getBearing() + dBearing * rotationSpeed * dt)
        }

        window.requestAnimationFrame(::frame)
    }

    window.requestAnimationFrame(::frame)
}