package streetlight.web.ui

import koala.dom.*
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.model.data.GalaxyId
import streetlight.web.model.Streetlight

//fun ViewContext<Streetlight>.wireGalaxyLights(root: HTMLElement) {
//    val app = model
//    val cache = app.cache.galaxy
//    val pairs = root.queryAttributeAll(StarLightKey.GalaxyLightId) { GalaxyId(it) }
//
//    renderScope.launch {
//        cache.lightFlow.collect { lights ->
//            pairs.forEach { (element, galaxyId) ->
//                val isLit = lights.any { it == galaxyId }
//                element.setAttribute(StarLightKey.IsLit.to(isLit))
//            }
//        }
//    }
//
//    pairs.forEach { (element, galaxyId) ->
//        element.onClick {
//            cache.toggleLight(galaxyId)
//        }
//    }
//}