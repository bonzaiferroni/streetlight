package streetlight.web.interop

import kabinet.utils.toMetricString
import koala.core.queryFirstOrNull
import koala.dom.ViewScope
import koala.dom.querySelectorAll
import koala.modifier.requireAttribute
import koala.dom.requireClosest
import koala.modifier.setAttribute
import koala.modifier.Attribute
import streetlight.model.data.LightEdit
import streetlight.web.model.DataCache
import streetlight.web.ui.StarToggle
import streetlight.web.ui.api
import streetlight.web.ui.starCheck
import web.dom.document
import web.html.HTMLElement

fun ViewScope.toggleAny(element: HTMLElement) {
    starCheck { return }

    val base = element.requireClosest(StarToggle.Class)
    val uuid = base.requireAttribute(StarToggle.ToggleId)
    val toggleType = base.requireAttribute(StarToggle.TypeData)
    val isOn = !base.requireAttribute(Attribute.IsOn)
    document.querySelectorAll(StarToggle.ToggleId.to(uuid)).forEach { toggleElement ->
        toggleElement.setAttribute(Attribute.IsOn.to(isOn))
        val counter = toggleElement.queryFirstOrNull(StarToggle.Counter) ?: error("counter not found")
        counter.textContent?.toIntOrNull()?.let {
            val count = if (isOn) it + 1 else it - 1
            counter.textContent = count.toMetricString()
        }
    }
    launchEffect {
        api.star.editStarLink(LightEdit(uuid, isOn, toggleType))
    }
}

fun ViewScope.toggleGalaxy(element: HTMLElement) {
    toggleAny(element)
    app.get<DataCache>().galaxyFleet.clear()
}