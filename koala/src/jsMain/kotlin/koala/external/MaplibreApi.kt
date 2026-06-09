@file:Suppress("unused")

package koala.external

import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement

external interface Evented {
    fun off(type: String, listener: Listener): Subscription
    fun on(type: String, listener: Listener): Subscription
    fun listens(type: String): Boolean
    fun setEventedParent(parent: Evented?, data: dynamic): Evented
}

external interface Alignment
external interface Subscription

// Type aliases in TS; keep loose for Kotlin/JS interop.
external interface LngLatLike
external interface PointLike

external object maplibregl {

    class Map(options: MapOptions) : Evented {
        // Methods
        fun addControl(control: dynamic, position: ControlPosition = definedExternally): Map
        fun removeControl(control: dynamic): Map

        fun getContainer(): HTMLElement
        fun getStyle(): StyleSpecification
        fun setStyle(style: StyleSpecification): Map

        fun getZoom(): Double
        fun setZoom(zoom: Double): Map

        fun getCenter(): LngLat
        fun setCenter(center: LngLatLike): Map
        fun setCenter(center: LngLat): Map

        fun panTo(center: LngLat): Map
        fun panBy(offset: PointLike, options: dynamic = definedExternally): Map
        fun easeTo(options: dynamic): Map
        fun getBearing(): Double
        fun setBearing(bearing: Double): Map
        fun getPitch(): Double
        fun setPitch(pitch: Double): Map
        fun flyTo(options: CenterZoomBearing): Map
        fun jumpTo(options: CenterZoomBearing): Map

        fun getBounds(): LngLatBounds
        fun fitBounds(bounds: LngLatBounds, options: dynamic = definedExternally): Map

        fun resize(): Map
        fun loaded(): Boolean
        fun isStyleLoaded(): Boolean

        override fun listens(type: String): Boolean
        override fun setEventedParent(parent: Evented?, data: dynamic): Evented
        override fun off(type: String, listener: Listener): Subscription
        override fun on(type: String, listener: Listener): Subscription

        fun addSource(id: String, source: MapSource)
        fun removeSource(id: String)
        fun getSource(id: String): Source
        fun addLayer(layer: dynamic, layerId: dynamic)
        fun addLayer(layer: dynamic)
        fun removeLayer(id: String)

        fun project(point: LngLat): Point

        fun setTerrain(terrain: dynamic)

        fun setLayoutProperty(layerId: String, propertyName: String, value: String)

        // Event handling inherited from .koala.external.Evented
    }

    class Marker(
        options: MarkerOptions? = definedExternally
    ) : Evented {
        fun addClassName(className: String)
        fun addTo(map: Map): Marker /* returns `this` */

        fun getElement(): HTMLElement
        fun getLngLat(): LngLat
        fun getOffset(): Point
        fun getPitchAlignment(): Alignment
        fun getPopup(): Popup
        fun getRotation(): Double
        fun getRotationAlignment(): Alignment

        fun isDraggable(): Boolean

        override fun listens(type: String): Boolean
        override fun off(type: String, listener: Listener): Subscription
        override fun on(type: String, listener: Listener): Subscription
        override fun setEventedParent(parent: Evented?, data: dynamic): Evented
        fun once(type: String, listener: Listener = definedExternally): dynamic /* Promise<any> | ..Marker */

        fun remove(): Marker /* returns `this` */
        fun removeClassName(className: String)

        fun setDraggable(shouldBeDraggable: Boolean = definedExternally): Marker /* returns `this` */
        // fun setEventedParent(parent: Evented = definedExternally, data: dynamic = definedExternally): Marker

        fun setLngLat(lnglat: LngLat): Marker /* returns `this` */
        fun setOffset(offset: PointLike): Marker /* returns `this` */

        fun setOpacity(opacity: String = definedExternally, opacityWhenCovered: String = definedExternally): Marker /* returns `this` */

        fun setPitchAlignment(alignment: Alignment = definedExternally): Marker /* returns `this` */
        fun setPopup(popup: Popup = definedExternally): Marker /* returns `this` */

        fun setRotation(rotation: Double = definedExternally): Marker /* returns `this` */
        fun setRotationAlignment(alignment: Alignment = definedExternally): Marker /* returns `this` */

        fun setSubpixelPositioning(value: Boolean): Marker

        fun toggleClassName(className: String): Boolean
        fun togglePopup(): Marker /* returns `this` */
    }

    class LngLat(
        val lng: Double,
        val lat: Double
    ) {
        fun distanceTo(lngLat: LngLat): Double
    }

    class LngLatBounds(
        val sw: LngLat,
        val ne: LngLat,
    ) {
        fun getCenter(): LngLat
        fun getEast(): Double
        fun getNorth(): Double
        fun getNorthEast(): LngLat
        fun getNorthWest(): LngLat
        fun getSouth(): Double
        fun getSouthEast(): LngLat
        fun getSouthWest(): LngLat
        fun getWest(): Double
    }

    class Popup
    class Point(
        val x: Double,
        val y: Double
    ) : PointLike

    class NavigationControl
    class FullscreenControl
}

typealias Listener = (dynamic) -> Unit

//fun maplibregl.Marker.on(type: String, listener: () -> Unit): Subscription {
//    return this.on(type, maplibregl.Listener(listener))
//}

@JsPlainObject
external interface MarkerOptions {
    val anchor: PositionAnchor?
    val className: String?
    val clickTolerance: Double?
    val color: String?
    val draggable: Boolean?
    val element: HTMLElement?
    val offset: PointLike?
    val opacity: String?
    val opacityWhenCovered: String?
    val pitchAlignment: Alignment?
    val rotation: Double?
    val rotationAlignment: String?
    val scale: Double?
    val subpixelPositioning: Boolean?
}

external interface Source {
    fun setData(data: dynamic)
}

// Stub for .koala.external.PositionAnchor
external interface PositionAnchor

// Stubs for types not defined yet
@JsPlainObject
external interface MapOptions {
    val container: HTMLElement?
    val style: Any?
    val center: maplibregl.LngLat
    val zoom: Number
    val pitch: Number?
    val bearing: Number?
    val canvasContextAttributes: CanvasContextAttributes?
}

@JsPlainObject
external interface TerrainSpec {
    val source: String
    val exaggeration: Number?
}

external interface Camera
external interface ControlPosition
external interface GestureOptions
external interface DragPanOptions
external interface AroundCenterOptions

@JsPlainObject
external interface CenterZoomBearing {
    var center: maplibregl.LngLat?
    var zoom: Double?
    var bearing: Double?
    var pitch: Double?
    var easing: ((Double) -> Double)?
}

typealias Expression = Array<Any?>

@JsPlainObject
external interface MapLayer {
    val id: String
    val type: String                    // "raster" | "hillshade" | ...
    val source: String?
    @JsName("source-layer")
    var sourceLayer: String?
    val minzoom: Number?
    val maxzoom: Number?
    val layout: Any?
    val paint: Any?                     // keys like "hillshade-shadow-color" sail as Any
    val filter: Array<Any>?
    val beforeId: String?
}

@JsPlainObject
external interface FillExtrusionPaint {
    @JsName("fill-extrusion-color")
    var fillExtrusionColor: Any? // String | Expression

    @JsName("fill-extrusion-height")
    var fillExtrusionHeight: Any? // Number | Expression

    @JsName("fill-extrusion-base")
    var fillExtrusionBase: Any? // Number | Expression

    @JsName("fill-extrusion-opacity")
    var fillExtrusionOpacity: Any? // Number | Expression
}

@JsPlainObject
external interface MapSource {
    var type: String
    var url: String?
    var tiles: Array<String>?
    var minzoom: Number?
    var maxzoom: Number?
    var data: dynamic /* String | GeoJSON */
    val encoding: String?
    val tileSize: Number?
    val attribution: String?
}

@JsPlainObject
external interface MapSources {
    val osm: MapSource?
    val terrainSource: MapSource?
    val hillshadeSource: MapSource?
}

@JsPlainObject
external interface CanvasContextAttributes {
    var antialias: Boolean?
    var alpha: Boolean?
    var depth: Boolean?
    var stencil: Boolean?
    var premultipliedAlpha: Boolean?
    var preserveDrawingBuffer: Boolean?
    var failIfMajorPerformanceCaveat: Boolean?
}

@JsPlainObject
external interface StyleSpecification {
    var layers: Array<StyleLayer>?
}

@JsPlainObject
external interface StyleLayer {
    var id: String
    var type: String
    var layout: LayerLayout?
}

@JsPlainObject
external interface LayerLayout {
    @JsName("text-field")
    var textField: Any?
}

@JsPlainObject
external interface FeatureCollection {
    var type: String /* "FeatureCollection" */
    var features: Array<Feature>
}

@JsPlainObject
external interface Feature {
    var type: String /* "Feature" */
    var properties: dynamic /* plain object */
    var geometry: Geometry
}

@JsPlainObject
external interface Geometry {
    var type: String
}

@JsPlainObject
external interface LineStringGeometry : Geometry {
    var coordinates: Array<Array<Double>> /* [lng, lat][] */
}

@JsPlainObject
external interface LayerSpecification {
    var id: String
    var type: String
    var source: String
    var paint: LinePaint?
    var layout: LineLayout?
}

@JsPlainObject
external interface LinePaint {
    @JsName("line-color")
    var lineColor: String?

    @JsName("line-width")
    var lineWidth: Number?
}

@JsPlainObject
external interface LineLayout {
    @JsName("line-join")
    var lineJoin: String? /* "miter" | "bevel" | "round" */

    @JsName("line-cap")
    var lineCap: String? /* "butt" | "round" | "square" */
}

@JsPlainObject
external interface MapStyle {
    val version: Number                 // must be 8
    val name: String?
    val sources: dynamic                    // map keyed by arbitrary source IDs
    val layers: Array<MapLayer>
    val terrain: MapTerrain?
    val sprite: String?
    val glyphs: String?
    val center: Array<Number>?
    val zoom: Number?
    val bearing: Number?
    val pitch: Number?
}

@JsPlainObject
external interface MapTerrain {
    val source: String
    val exaggeration: Number?
}

@JsPlainObject
external interface RasterSource {
    val type: String                    // "raster"
    val tiles: Array<String>?
    val url: String?
    val tileSize: Number?
    val attribution: String?
    val maxzoom: Number?
    val minzoom: Number?
}

@JsPlainObject
external interface RasterDemSource {
    val type: String                    // "raster-dem"
    val tiles: Array<String>?
    val url: String?
    val tileSize: Number?
    val maxzoom: Number?
    val minzoom: Number?
    val encoding: String?               // "terrarium" | "mapbox"
}

@JsPlainObject
external interface MapLayerLayout {
    val visibility: String?             // "visible" | "none"
}

@JsPlainObject
external interface HillshadePaint {
    @JsName("hillshade-shadow-color")
    val hillshadeShadowColor: String?
    @JsName("hillshade-exaggeration")
    val hillshadeExaggeration: Any?
}