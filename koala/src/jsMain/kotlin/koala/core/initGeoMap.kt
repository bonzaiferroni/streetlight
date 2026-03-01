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
        style = "/www/misc/fjord",
        center = center,
        zoom = 11,
        pitch = 45,
        canvasContextAttributes = js("{ antialias: true }")
    ))

    widget.addControl(maplibregl.NavigationControl())
    widget.addControl(maplibregl.FullscreenControl())

    window.asDynamic().widget = widget


    widget.on("load") {
        val layers = widget.getStyle().layers.unsafeCast<Array<dynamic>>()

        var labelLayerId: String? = null
        for (i in layers.indices) {
            val layer = layers[i]
            if (layer.type == "symbol" && layer.layout != null && layer.layout["text-field"] != null) {
                labelLayerId = layer.id as String
                break
            }
        }

        widget.addSource(
            "openfreemap",
            js(
                """
            ({
                type: "vector",
                url: "https://tiles.openfreemap.org/planet"
            })
            """
            )
        )

        val layerDef = js(
            """
    ({
        id: "3d-buildings",
        source: "openfreemap",
        "source-layer": "building",
        type: "fill-extrusion",
        minzoom: 15,
        filter: ["!=", ["get", "hide_3d"], true],
        paint: {
            "fill-extrusion-color": [
                "interpolate",
                ["linear"],
                ["coalesce", ["get", "render_height"], 0],

                0,   "hsla(232,47%,18%,0.65)",  
                6,   "hsl(224,22%,45%)",       
                60,  "hsl(224,20%,34%)",
                200, "hsl(224,20%,24%)",
                500, "hsl(224,22%,16%)"       
            ],
            "fill-extrusion-height": [
                "interpolate",
                ["linear"],
                ["zoom"],
                15, 0,
                16, ["get", "render_height"]
            ],
            "fill-extrusion-base": [
                "interpolate",
                ["linear"],
                ["zoom"],
                15, 0,
                16, ["get", "render_min_height"]
            ],
            "fill-extrusion-opacity": [
                "interpolate",
                ["linear"],
                ["zoom"],
                15, 0.0,
                16, 0.55
            ]
        }
    })
    """
        )

        if (labelLayerId != null) {
            widget.addLayer(layerDef, labelLayerId)
        } else {
            widget.addLayer(layerDef)
        }
    }

    console.log("assigning map window")
    return window
}