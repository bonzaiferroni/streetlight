@file:Suppress("unused")

package streetlight.web

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
        fun getStyle(): dynamic /* .streetlight.web.StyleSpecification */
        fun setStyle(style: dynamic /* .streetlight.web.StyleSpecification | String */): Map

        fun getZoom(): Double
        fun setZoom(zoom: Double): Map

        fun getCenter(): LngLat
        fun setCenter(center: LngLatLike): Map

        fun flyTo(options: dynamic): Map
        fun getBounds(): LngLatBounds
        fun fitBounds(bounds: LngLatBounds, options: dynamic = definedExternally): Map

        fun resize(): Map

        override fun listens(type: String): Boolean
        override fun setEventedParent(parent: Evented?, data: dynamic): Evented
        override fun off(type: String, listener: Listener): Subscription
        override fun on(type: String, listener: Listener): Subscription

        fun addSource(id: String, source: SourceSpecification)
        fun addLayer(layer: dynamic)

        // Event handling inherited from .streetlight.web.Evented
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
    class Point

    class NavigationControl
    class FullscreenControl
}

typealias Listener = (dynamic) -> Unit

//fun maplibregl.Marker.on(type: String, listener: () -> Unit): Subscription {
//    return this.on(type, maplibregl.Listener(listener))
//}

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

external interface SourceSpecification

external interface AddLayerObject

// Stub for .streetlight.web.PositionAnchor
external interface PositionAnchor

// Stubs for types not defined yet
external interface MapOptions
external interface Camera
external interface StyleSpecification
external interface ControlPosition
external interface GestureOptions
external interface DragPanOptions
external interface AroundCenterOptions
