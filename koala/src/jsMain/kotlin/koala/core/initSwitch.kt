package koala.core

import koala.css.ElementClass
import koala.dom.onCustomEvent
import koala.dom.sendCustomEvent
import koala.dom.setAttribute
import koala.html.Attribute
import koala.html.ElementEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

fun findAndInitSwitches(ancestor: HTMLElement) {
    val elements = ancestor.querySelectorAll(ElementClass.switch.selector).asList()
    elements.forEach { element ->
        initSwitch(element as HTMLElement)
    }
}

fun initSwitch(
    element: HTMLElement,
) {
    var isOn = element.attributes[Attribute.isOn]?.toBooleanStrictOrNull() ?: false

    fun setOn(value: Boolean) {
        if (value == isOn) return
        isOn = value
        element.setAttribute(Attribute.isOn, value.toString())
        element.setAttribute("aria-checked", value.toString())
        element.sendCustomEvent(ElementEvent.onToggle, isOn)
    }

    element.addEventListener("click", { setOn(!isOn) })
    element.addEventListener("keydown", { ev ->
        val key = (ev as? org.w3c.dom.events.KeyboardEvent)?.key
        if (key == "Enter" || key == " ") {
            ev.preventDefault()
            setOn(!isOn)
        }
    })
    element.onCustomEvent(ElementEvent.onToggle) {
        setOn(it)
    }
}