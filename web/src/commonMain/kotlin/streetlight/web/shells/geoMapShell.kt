package streetlight.web.shells

import koala.css.Height48
import koala.css.ModifierSet
import koala.css.Width100
import koala.css.modify
import koala.html.Id
import koala.html.box
import kotlinx.html.FlowContent

fun FlowContent.geoMapShell(
    modifiers: ModifierSet? = modify(Width100, Height48),
) {
    box(GeoMapId.mapMount, modifiers) {
        box(GeoMapId.window) {
            box(GeoMapId.widget)
            box(GeoMapId.overlay) {
                box(GeoMapId.crosshairs)
            }
        }
    }
}

object GeoMapId {
    val mapMount = Id("map-mount")
    val window = Id("map-window")
    val widget = Id("map-widget")
    val overlay = Id("map-overlay")
    val crosshairs = Id("map-crosshairs")
    val panel = Id("map-panel")
}