@file:OptIn(ExperimentalJsExport::class)

package koala.core

import koala.external.MapOptions
import koala.external.maplibregl
import koala.html.GeoMapSelector
import org.w3c.dom.HTMLElement

var geoMapWindow: HTMLElement? = null

fun findAndInitGeoMap(ancestor: HTMLElement) {
    if (geoMapWindow != null) {
        console.log("geomap already initialized")
        return
    }
    val mounts = ancestor.queryAll(GeoMapSelector.mapMount)
    if (mounts.isEmpty()) return
    if (mounts.size > 1) {
        console.log("Warning: more than one geomap mount found, using first")
    }
    val mount = mounts[0] as? HTMLElement ?: return
    initGeoMap(mount)
}

fun initGeoMap(mount: HTMLElement) {
    console.log("initializing geomap")
    val window = mount.appendDiv(GeoMapSelector.window)
    val widgetBox = window.appendDiv(GeoMapSelector.widget)
    val overlay = window.appendDiv(GeoMapSelector.overlay)
    overlay.appendDiv(GeoMapSelector.crosshairs)

    val widget = maplibregl.Map(MapOptions(
        container = widgetBox,
        style = "https://tiles.openfreemap.org/styles/fiord",
        center = maplibregl.LngLat(-104.95, 39.75),
        zoom = 11
    ))

    widget.addControl(maplibregl.NavigationControl())
    widget.addControl(maplibregl.FullscreenControl())

    geoMapWindow = window

    window.asDynamic().widget = widget

    console.log("assigning map window")
}