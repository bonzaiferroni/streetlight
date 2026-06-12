package koala.dom

import kampfire.model.GeoPoint
import koala.css.*
import koala.html.configureGeoMapMount
import koala.model.GeoCameraController
import kotlinx.html.DIV

fun RenderScope.geoMapMount(
    initialPoint: GeoPoint? = null,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
): GeoCameraController {
    val element = box {
        configureGeoMapMount(initialPoint, mod, block)
    }

    return wireGeoMap(element)
}