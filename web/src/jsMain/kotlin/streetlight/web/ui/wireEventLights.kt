package streetlight.web.ui

import koala.core.queryFirstOrNull
import koala.css.Property
import koala.css.UrlValue
import koala.dom.onClick
import koala.dom.queryAttributeAll
import koala.dom.setAttribute
import koala.html.IconKey
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.model.data.EventId
import streetlight.web.model.Streetlight

//fun ViewContext<Streetlight>.wireEventLights(root: HTMLElement) {
//    val app = model
//    val cache = app.cache.event
//    val pairs = root.queryAttributeAll(LightKey.EventLightId) { EventId(it) }
//
//    renderScope.launch {
//        cache.lightFlow.collect { lights ->
//            pairs.forEach { (element, eventId) ->
//                val isLit = lights.any { it.eventId == eventId }
//                element.setAttribute(LightKey.LightStatus.to(isLit))
//            }
//        }
//    }
//
//    pairs.forEach { (element, eventId) ->
//        element.onClick {
//            val light = cache.stateNow.lights.firstOrNull { it.eventId == eventId}?.let { star ->
//                star.copy(value = when (star.value) {
//                    null -> LightType.Light
//                    else -> null
//                })
//            } ?: EventLight(eventId, LightType.Light)
//
//            cache.editLight(light)
//        }
//    }
//}