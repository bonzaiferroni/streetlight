@file:Suppress("unused")

package streetlight.web

import org.w3c.dom.HTMLElement

external open class Evented
external open class Popup
external open class Point

external interface Alignment
external interface Subscription
external interface Listener

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
        fun fitBounds(bounds: dynamic, options: dynamic = definedExternally): Map

        fun resize(): Map

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

        fun listens(type: String): Boolean
        fun off(type: String, listener: Listener): Marker
        fun on(type: String, listener: Listener): Subscription
        fun once(type: String, listener: Listener = definedExternally): dynamic /* Promise<any> | ..Marker */

        fun remove(): Marker /* returns `this` */
        fun removeClassName(className: String)

        fun setDraggable(shouldBeDraggable: Boolean = definedExternally): Marker /* returns `this` */
        fun setEventedParent(parent: Evented = definedExternally, data: dynamic = definedExternally): Marker

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
}

data class MarkerOptions(
    val anchor: PositionAnchor? = null,
    val className: String? = null,
    val clickTolerance: Double? = null,
    val color: String? = null,
    val draggable: Boolean? = null,
    val element: HTMLElement? = null,
    val offset: PointLike? = null,
    val opacity: String? = null,
    val opacityWhenCovered: String? = null,
    val pitchAlignment: Alignment? = null,
    val rotation: Double? = null,
    val rotationAlignment: String? = null,
    val scale: Double? = null,
    val subpixelPositioning: Boolean? = null,
)

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
