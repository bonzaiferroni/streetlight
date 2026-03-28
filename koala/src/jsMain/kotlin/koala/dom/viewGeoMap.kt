package koala.dom

import kampfire.model.GeoPoint
import koala.css.*
import koala.html.geoMapMount
import koala.model.GeoMap
import kotlinx.coroutines.CoroutineScope

fun RenderContext.viewGeoMap(
    geoMap: GeoMap,
    appScope: CoroutineScope,
    initialPoint: GeoPoint? = null,
    modifiers: ModifierSet? = modify(Width100P, Height48),
) {
    val element = box(modifiers) {
        geoMapMount(initialPoint, modify(Width100P, Height100P))
    }

    wireGeoMap(geoMap, appScope, element)
}