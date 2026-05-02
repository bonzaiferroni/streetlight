package streetlight.web.ui

import koala.css.Magic
import koala.dom.ViewContext
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

fun <Id> ViewContext<Streetlight>.wireLights(
    root: HTMLElement,
    attribute: Attribute<Id>,
    cache: LightCache<Id, *>
) {
    val app = model
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

        launch {
            // increment count on Beacon
            app.omni.beaconFlow.collect { beacon ->
                val (element, _) = pairs.firstOrNull { it.value.toString() == beacon.itemId } ?: return@collect
                element.modifyCounter(1)
                element.trigger(Magic)
            }
        }
    }

    pairs.forEach { (element, id) ->
        element.onClick {
            cache.toggleLight(id)
        }
    }
}

private fun HTMLElement.modifyCounter(delta: Int) {
    val counterElement = querySelector(StarLightKey.LightCounter) ?: return
    val currentCount = counterElement.textContent?.toIntOrNull() ?: 0
    counterElement.textContent = (currentCount + delta).coerceIn(0, Int.MAX_VALUE).toString()
}