package koala.dom

import kampfire.model.GeoPoint
import koala.css.*
import koala.html.configureGeoMapMount
import koala.model.GeoMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.html.DIV
import org.w3c.dom.HTMLElement

fun RenderContext.geoMapMount(
    geoMap: GeoMap,
    appScope: CoroutineScope,
    initialPoint: GeoPoint? = null,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
): HTMLElement {
    val element = box {
        configureGeoMapMount(initialPoint, mod, block)
    }

    wireGeoMap(geoMap, appScope, element)

    return element
}