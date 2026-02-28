@file:OptIn(ExperimentalJsExport::class)

package koala.core

import koala.external.MapOptions
import koala.external.maplibregl
import koala.html.GeoMapSelector
import org.w3c.dom.HTMLElement

// globalThis.geoMapWindow: HTMLElement? = null
external val globalThis: dynamic

fun findAndInitGeoMap(ancestor: HTMLElement): HTMLElement? {
    val geoMapWindow = globalThis.geoMapWindow as? HTMLElement
    geoMapWindow?.let {
        console.log("geomap already initialized")
        return it
    }

    val mounts = ancestor.queryAll(GeoMapSelector.mapMount)
    if (mounts.isEmpty()) return null
    if (mounts.size > 1) {
        console.log("Warning: more than one geomap mount found, using first")
    }
    val mount = mounts[0] as? HTMLElement ?: return null
    val element = initGeoMap(mount)
    globalThis.geoMapWindow = element
    return element
}

fun initGeoMap(mount: HTMLElement): HTMLElement {
    console.log("initializing geomap!")
    val window = mount.appendDiv(GeoMapSelector.window)
    val widgetBox = window.appendDiv(GeoMapSelector.widget)
    val overlay = window.appendDiv(GeoMapSelector.overlay)
    overlay.appendDiv(GeoMapSelector.crosshairs)
    overlay.appendDiv(GeoMapSelector.focusPanel)

    val center = mount
        .getAttribute(GeoMapSelector.geoPoint.value)
        ?.split(",")
        ?.mapNotNull { it.toDoubleOrNull() }
        ?.takeIf { it.size == 2 }
        ?.let { (lng, lat) -> maplibregl.LngLat(lng, lat) }
        ?: maplibregl.LngLat(-104.95, 39.75)

    val widget = maplibregl.Map(MapOptions(
        container = widgetBox,
        style = "https://tiles.openfreemap.org/styles/fiord",
        center = center,
        zoom = 11
    ))

    widget.addControl(maplibregl.NavigationControl())
    widget.addControl(maplibregl.FullscreenControl())

    window.asDynamic().widget = widget

    console.log("assigning map window")
    return window
}