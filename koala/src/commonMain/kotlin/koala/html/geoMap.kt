package koala.html

import kampfire.model.GeoPoint
import koala.css.Class
import koala.css.Height48
import koala.css.ModifierSet
import koala.css.Width100P
import koala.css.addModifiers
import koala.css.modify
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.geoMapMount(
    initialPoint: GeoPoint? = null,
    modifiers: ModifierSet? = null, // modify(Width100P, Height48)
    block: DIV.() -> Unit = {}
) {
    box {
        addModifiers(GeoMapSelector.mapMount, modifiers)
        initialPoint?.let {
            setAttribute(GeoMapSelector.geoPoint, "${it.lng},${it.lat}")
        }
        block()
    }
}

object GeoMapSelector {
    val mapMount = Class("map-mount")
    val window = Id("map-window")
    val widget = Id("map-widget")
    val overlay = Id("map-overlay")
    val crosshairs = Id("map-crosshairs")
    val panel = Id("map-panel")
    val geoPoint = Attribute<String>("data-geo-point")
    val focusPanel = Id("map-focus-panel")
}