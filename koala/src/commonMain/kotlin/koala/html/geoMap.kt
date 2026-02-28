package koala.html

import kampfire.model.GeoPoint
import koala.css.Css
import koala.css.Height48
import koala.css.ModifierSet
import koala.css.Width100
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.html.FlowContent

fun FlowContent.geoMapMount(
    initialPoint: GeoPoint? = null,
    modifiers: ModifierSet? = modify(Width100, Height48),
) {
    box {
        applyModifiers(GeoMapSelector.mapMount, modifiers)
        initialPoint?.let {
            attributes[GeoMapSelector.geoPoint] = "${it.lng},${it.lat}"
        }
    }
}

object GeoMapSelector {
    val mapMount = Css("map-mount")
    val window = Id("map-window")
    val widget = Id("map-widget")
    val overlay = Id("map-overlay")
    val crosshairs = Id("map-crosshairs")
    val panel = Id("map-panel")
    val geoPoint = Attribute("data-geo-point")
    val focused = Css("map-focus")
    val focusPanel = Id("map-focus-panel")
}