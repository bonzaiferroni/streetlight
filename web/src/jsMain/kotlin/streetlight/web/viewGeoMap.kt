package streetlight.web

import koala.css.Css
import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.box
import koala.dom.modify
import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.html.*
import org.w3c.dom.HTMLDivElement

fun RenderContext.viewGeoMap(
    height: String = "400px",
): HTMLDivElement? {
    box(GeoMapIds.window, modify(Width100)) {
        style = "height: $height;"
        box(GeoMapIds.widget) {
        }
        box(GeoMapIds.overlay) {
            box(GeoMapIds.crosshairs)
        }
    }

    return null
}

object GeoMapIds {
    val window = Id("map-window")
    val widget = Id("map-widget")
    val overlay = Id("map-overlay")
    val crosshairs = Id("map-crosshairs")
    val panel = Id("map-panel")
}