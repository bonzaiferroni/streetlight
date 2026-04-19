@file:OptIn(ExperimentalJsExport::class)

package koala.core

import koala.external.FillExtrusionPaint
import koala.external.HillshadePaint
import koala.external.MapLayer
import koala.external.MapLayerLayout
import koala.external.MapOptions
import koala.external.MapSource
import koala.external.MapTerrain
import koala.external.maplibregl
import koala.html.GeoMapSelector
import kotlinx.browser.localStorage
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
    val overlay = widgetBox.appendDiv(GeoMapSelector.overlay)
    overlay.appendDiv(GeoMapSelector.crosshairs)
    overlay.appendDiv(GeoMapSelector.FocusPanel)

    var zoom: Number = 11
    val center = mount
        .getAttribute(GeoMapSelector.geoPoint.key)
        ?.split(",")
        ?.mapNotNull { it.toDoubleOrNull() }
        ?.takeIf { it.size == 2 }
        ?.let { (lng, lat) -> maplibregl.LngLat(lng, lat) }
        ?: cachedCenterPoint()?.also { zoom = cachedZoom() ?: zoom }
        ?: maplibregl.LngLat(-104.95, 39.75)

    val widget = maplibregl.Map(
        MapOptions(
            container = widgetBox,
            style = "/www/misc/fjord.json",
            center = center,
            zoom = zoom,
            pitch = 45,
//            canvasContextAttributes = CanvasContextAttributes(antialias = true)
        )
    )

    widget.addControl(maplibregl.NavigationControl())
    // widget.addControl(maplibregl.FullscreenControl())

    window.asDynamic().widget = widget


    widget.on("load") {

        val layers = widget.getStyle().layers.orEmpty()

        val labelLayerId = layers.firstOrNull { it.type == "symbol" && it.layout?.textField != null }?.id

        val source = MapSource(
            type = "vector",
            url = "https://tiles.openfreemap.org/planet"
        )

        widget.addSource("openfreemap", source)

        val layer = MapLayer(
            id = "3d-buildings",
            source = "openfreemap",
            sourceLayer = "building",
            type = "fill-extrusion",
            minzoom = 14.0,
            filter = arrayOf("!=", arrayOf("get", "hide_3d"), true),
            paint = FillExtrusionPaint(
                fillExtrusionColor = arrayOf(
                    "interpolate", arrayOf("linear"),
                    arrayOf("coalesce", arrayOf("get", "render_height"), 0),
                    0, "hsla(232,47%,18%,0.65)",
                    6, "hsl(224,22%,45%)",
                    60, "hsl(224,20%,34%)",
                    200, "hsl(224,20%,24%)",
                    500, "hsl(224,22%,16%)"
                ),
                fillExtrusionHeight = arrayOf(
                    "interpolate", arrayOf("linear"), arrayOf("zoom"),
                    14, 0,
                    15, arrayOf("get", "render_height")
                ),
                fillExtrusionBase = arrayOf(
                    "interpolate", arrayOf("linear"), arrayOf("zoom"),
                    14, 0,
                    15, arrayOf("get", "render_min_height")
                ),
                fillExtrusionOpacity = arrayOf(
                    "interpolate", arrayOf("linear"), arrayOf("zoom"),
                    14, 0.0,
                    15, 0.55,
                    16, 0.75
                )
            )
        )

        if (labelLayerId != null) {
            widget.addLayer(layer, labelLayerId)
        } else {
            widget.addLayer(layer)
        }
    }

    widget.on("moveend") {
        val center = widget.getCenter()
        val zoom = widget.getZoom()
        localStorage.setItem(MAP_CENTER_LAT_KEY, center.lat.toString())
        localStorage.setItem(MAP_CENTER_LNG_KEY, center.lng.toString())
        localStorage.setItem(MAP_ZOOM_KEY, zoom.toString())
    }

    console.log("assigning map window")
    return window
}

private fun cachedCenterPoint(): maplibregl.LngLat? {
    val lat = localStorage.getItem(MAP_CENTER_LAT_KEY)?.toDoubleOrNull()
    val lng = localStorage.getItem(MAP_CENTER_LNG_KEY)?.toDoubleOrNull()
    if (lat != null && lng != null) {
        return maplibregl.LngLat(lng, lat)
    }
    return null
}

private fun cachedZoom(): Double? {
    return localStorage.getItem(MAP_ZOOM_KEY)?.toDoubleOrNull()
}

private const val MAP_CENTER_LAT_KEY = "geomap.center.lat"
private const val MAP_CENTER_LNG_KEY = "geomap.center.lng"
private const val MAP_ZOOM_KEY = "geomap.zoom"

fun addTerrain(widget: maplibregl.Map) {
    widget.addSource("dem", MapSource(
        type = "raster-dem",
        tiles = arrayOf("https://s3.amazonaws.com/elevation-tiles-prod/terrarium/{z}/{x}/{y}.png"),
        encoding = "terrarium",
        tileSize = 256,
        maxzoom = 12
    ))
    widget.addLayer(
        MapLayer(
            id = "hills",
            type = "hillshade",
            source = "dem",
            layout = MapLayerLayout(visibility = "visible"),
            paint = HillshadePaint(
                hillshadeShadowColor = "#222222",
                hillshadeExaggeration = arrayOf(
                    "interpolate", arrayOf("linear"), arrayOf("zoom"),
                    8, 0.0,
                    10, 0.5
                )
            )
        ),
    )
    widget.setTerrain(
        MapTerrain(
            source = "dem",
            exaggeration = 1
        )
    )
}