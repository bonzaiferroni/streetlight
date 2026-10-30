package koala.dom.component

import kampfire.model.GeoPoint
import koala.modifier.*
import koala.model.GeoCameraController
import koala.model.MarkerMap
import kotlinx.html.DIV
import koala.dom.ViewScope
import koala.interop.wireGeoMap
import koala.component.configureGeoMapMount

/** A map centered on [initialPoint] showing the layers of [markerMap], returning the controller that moves its camera. */
fun ViewScope.geoMapMount(
    markerMap: MarkerMap,
    initialPoint: GeoPoint? = null,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
): GeoCameraController {
    val element = box {
        configureGeoMapMount(initialPoint, mod, block)
    }

    return wireGeoMap(element, markerMap)
}