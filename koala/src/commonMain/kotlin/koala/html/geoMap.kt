package koala.html

import kampfire.model.GeoPoint
import koala.css.Class
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.geoMapMount(
    initialPoint: GeoPoint? = null,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    box {
        configureGeoMapMount(initialPoint, modifiers, block)
    }
}

fun DIV.configureGeoMapMount(
    initialPoint: GeoPoint? = null,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    addModifiers(modifiers, GeoMapKey.MapMount)
    setAriaLabel("map window")
    initialPoint?.let {
        setAttribute(Attribute.GeoPointAttribute, initialPoint)
    }
    block()
}

object GeoMapKey {
    val MapMount = Class("map-mount")
    val Window = Id("map-window")
    val Widget = Id("map-widget")
    val Overlay = Id("map-overlay")
    // val Crosshairs = Id("map-crosshairs")
    val Panel = Id("map-panel")
    val FocusPanel = Id("map-focus-panel")
}

