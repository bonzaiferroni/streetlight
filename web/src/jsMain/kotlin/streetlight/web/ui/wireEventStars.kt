package streetlight.web.ui

import koala.core.queryFirstOrNull
import koala.css.Property
import koala.css.UrlValue
import koala.dom.ViewContext
import koala.dom.onClick
import koala.dom.queryAttributeAll
import koala.dom.setProperty
import koala.html.IconElement
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.model.data.EventId
import streetlight.model.data.EventStar
import streetlight.model.data.StarType
import streetlight.web.layouts.EventKey
import streetlight.web.layouts.iconPath
import streetlight.web.model.Streetlight

fun ViewContext<Streetlight>.wireEventStars(root: HTMLElement) {
    val app = model
    val cache = app.cache.event
    val pairs = root.queryAttributeAll(EventKey.EventStarId) { EventId(it) }

    renderScope.launch {
        cache.starFlow.collect { stars ->
            pairs.forEach { (element, eventId) ->
                val isStarred = stars.any { it.eventId == eventId }
                val star = when(isStarred) {
                    true -> EventStar(eventId, StarType.Star)
                    else -> EventStar(eventId, null)
                }

                element.queryFirstOrNull(IconElement.Class)?.style
                    ?.setProperty(Property.MaskUrl.to(UrlValue(star.value.iconPath)))
            }
        }
    }

    pairs.forEach { (element, eventId) ->
        element.onClick {
            val star = cache.stateNow.stars.firstOrNull { it.eventId == eventId}?.let { star ->
                star.copy(value = when (star.value) {
                    null -> StarType.Star
                    else -> null
                })
            } ?: EventStar(eventId, StarType.Star)

            cache.editStar(star)
        }
    }
}