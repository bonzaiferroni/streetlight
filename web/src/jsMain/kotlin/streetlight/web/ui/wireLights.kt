package streetlight.web.ui

import koala.css.Magic
import koala.dom.RenderContext
import koala.dom.modify
import koala.dom.onClick
import koala.dom.queryAttributeAll
import koala.dom.querySelector
import koala.dom.trigger
import koala.dom.unmodify
import koala.html.Attribute
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.web.model.LightCache
import streetlight.web.model.Streetlight

fun <Id> RenderContext.wireLights(
    root: HTMLElement,
    attribute: Attribute<Id>,
    cache: LightCache<Id, *>
) {
    val pairs = root.queryAttributeAll(attribute)

    renderScope.launch {
        launch {
            // modify flame
            cache.lightsFlow.collect { lights ->
                pairs.forEach { (element, itemId) ->
                    when (lights.any { it == itemId }) {
                        true -> element.modify(StarLightKey.IsLit)
                        else -> element.unmodify(StarLightKey.IsLit)
                    }
                }
            }
        }
    }

    pairs.forEach { (element, id) ->
        element.onClick {
            val isLit = cache.toggleLight(id)
            element.modifyCounter(if (isLit) 1 else -1)
        }
    }
}

private fun HTMLElement.modifyCounter(delta: Int) {
    val counterElement = querySelector(StarLightKey.LightCounter) ?: return
    val currentCount = counterElement.textContent?.toIntOrNull() ?: 0
    counterElement.textContent = (currentCount + delta).coerceIn(0, Int.MAX_VALUE).toString()
}