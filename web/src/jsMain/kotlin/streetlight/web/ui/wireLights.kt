package streetlight.web.ui

import koala.dom.ViewContext
import koala.dom.modify
import koala.dom.onClick
import koala.dom.queryAttributeAll
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
    val pairs = root.queryAttributeAll(attribute, cache.stringToId)

    renderScope.launch {
        cache.lightsFlow.collect { lights ->
            pairs.forEach { (element, galaxyId) ->
                when (lights.any { it == galaxyId }) {
                    true -> element.modify(StarLightKey.IsLit)
                    else -> element.unmodify(StarLightKey.IsLit)
                }
            }
        }
    }

    pairs.forEach { (element, id) ->
        element.onClick {
            cache.toggleLight(id)
        }
    }
}