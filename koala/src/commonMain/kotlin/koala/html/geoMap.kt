package koala.html

import kampfire.model.GeoPoint
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

/** The element the browser mounts a map into, centered on [initialPoint] when given. */
fun FlowContent.geoMapMount(
    initialPoint: GeoPoint? = null,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    box {
        configureGeoMapMount(initialPoint, mod, block)
    }
}

/** Configures this element as a [geoMapMount]. */
fun DIV.configureGeoMapMount(
    initialPoint: GeoPoint? = null,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    addModifiers(mod, GeoMapKey.MapMount)
    setAriaLabel("map window")
    initialPoint?.let {
        setAttribute(Attribute.GeoPointAttribute, initialPoint)
    }
    block()
}

/** The class and ids of the map's elements. */
object GeoMapKey {
    val MapMount = Class("map-mount")
    val Window = Id("map-window")
    val Widget = Id("map-widget")
    val Overlay = Id("map-overlay")
    val Crosshairs = Id("map-crosshairs")
    val Panel = Id("map-panel")
    val FocusPanel = Id("map-focus-panel")
}

