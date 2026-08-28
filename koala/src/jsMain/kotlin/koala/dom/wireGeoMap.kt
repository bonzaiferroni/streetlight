package koala.dom

import koala.core.findAndInitGeoMap
import koala.core.queryFirstOrNull
import koala.dom.getElementOrNullById
import koala.external.maplibregl
import koala.html.GeoMapKey
import koala.model.GeoCameraController
import koala.external.maplibregl.Point
import koala.model.GeoCamera
import koala.model.GeoMap
import koala.model.GeoRender
import kotlinx.coroutines.CoroutineScope
import web.animations.requestAnimationFrame
import web.dom.document
import web.events.addEventListener
import web.html.HTMLElement
import web.keyboard.*
import web.keyboard.KeyboardEvent
import web.performance.performance
import web.window.window

fun ViewScope.wireGeoMap(
    ancestor: HTMLElement,
): GeoCameraController {
    val mount = ancestor.takeIf { it.isModified(GeoMapKey.MapMount) }
        ?: ancestor.queryFirstOrNull(GeoMapKey.MapMount) ?: error("mount not found")
    val context = wireMapContext(mount)

    mount.onView { isVisible ->
        if (isVisible && mount.children.length == 0) {
            console.log("grabbing geomap window")
            mount.appendChild(context.windowElement)
//            val geoPoint = mount.getAttribute(GeoMapSelector.geoPoint.key)
//                ?.split(",")
//                ?.mapNotNull { it.toDoubleOrNull() }
//                ?.takeIf { it.size == 2 }
//                ?.let { GeoPoint(it[0], it[1]) }
//            geoPoint?.let {
//                geoMap.panMap(it)
//            }
            // mapWidget?.resize()
        }
    }

    return context
}

// private var cachedContext: GeoCameraController? = null

fun ViewScope.wireMapContext(
    mount: HTMLElement
): GeoCameraController {
    app.getOrNull<GeoCameraController>()?.let {
        return it
    }

    console.log("creating geomap")
    val camera = app.get<GeoCamera>()
    val geoMap = app.get<GeoMap>()
    val appScope = app.get<CoroutineScope>()
    val mapWindow = document.getElementOrNullById(GeoMapKey.Window) ?: findAndInitGeoMap(mount) ?: error("geomap window not found")
    val jsMap: maplibregl.Map = mapWindow.asDynamic().widget ?: error("geomap widget not found")
    val cameraController = GeoCameraController(jsMap, mapWindow, camera, appScope)
    val geoRender = GeoRender(mapWindow, jsMap, camera, appScope, geoMap)
    app.koin.declare(geoRender)
    app.koin.declare(cameraController)

    // wireKeyboardControls(widget)

    return cameraController
}

fun wireKeyboardControls(widget: maplibregl.Map) {
    val pressedKeys = mutableMapOf<KeyCode, Boolean>()

    window.addEventListener(KeyboardEvent.KEY_DOWN, { event ->
        if (event.altKey || event.ctrlKey || event.metaKey || event.shiftKey) return@addEventListener
        pressedKeys[event.code] = true
    })

    window.addEventListener(KeyboardEvent.KEY_UP, { event ->
        event as org.w3c.dom.events.KeyboardEvent
        pressedKeys[event.code] = false
    })

    // pixels the map pans per second
    val panSpeed = 500.0
    // degrees the map rotates per second
    val rotationSpeed = 100.0

    var lastTime = performance.now()

    fun frame(time: Double) {
        val dt = ((time - lastTime) / 1000.0).coerceAtMost(0.1)
        lastTime = time

        var dx = 0.0
        var dy = 0.0
        var dBearing = 0.0

        if (pressedKeys[KeyCode.KeyW] == true || pressedKeys[KeyCode.ArrowUp] == true) dy -= 1.0
        if (pressedKeys[KeyCode.KeyS] == true || pressedKeys[KeyCode.ArrowDown] == true) dy += 1.0
        if (pressedKeys[KeyCode.KeyA] == true || pressedKeys[KeyCode.ArrowLeft] == true) dx -= 1.0
        if (pressedKeys[KeyCode.KeyD] == true || pressedKeys[KeyCode.ArrowRight] == true) dx += 1.0
        if (pressedKeys[KeyCode.KeyQ] == true) dBearing -= 1.0
        if (pressedKeys[KeyCode.KeyE] == true) dBearing += 1.0

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

        requestAnimationFrame(::frame)
    }

    requestAnimationFrame(::frame)
}