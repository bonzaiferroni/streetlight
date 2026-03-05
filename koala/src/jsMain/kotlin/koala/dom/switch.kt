package koala.dom

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.js.span
import org.w3c.dom.HTMLDivElement

fun RenderContext.switch(
    label: String,
    modifiers: ModifierSet? = null,
    initialOn: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    bindFlow: Flow<Boolean>? = null,
    block: (DIV.() -> Unit)? = null,
): HTMLDivElement {
    var isOn = initialOn

    val element = div {
        applyModifiers(modify(ElementClass.switch, modifiers))
        attributes["role"] = "switch"
//        attributes["tabindex"] = "0"
        attributes["aria-checked"] = isOn.toString()
        attributes["data-on"] = isOn.toString()

        // ghost text defines the inner pill width; outer padding makes the “constraints” larger
        span("switch__ghost") { +label }
        span("switch__pill") { +label }

        block?.invoke(this)
    }

    fun setOn(value: Boolean) {
        isOn = value
        element.setAttribute("data-on", value.toString())
        element.setAttribute("aria-checked", value.toString())
        onToggle?.invoke(value)
    }

    element.addEventListener("click", { setOn(!isOn) })
    element.addEventListener("keydown", { ev ->
        val key = (ev as? org.w3c.dom.events.KeyboardEvent)?.key
        if (key == "Enter" || key == " ") {
            ev.preventDefault()
            setOn(!isOn)
        }
    })

    bindFlow?.let { flow ->
        renderScope.launch {
            flow.collect { setOn(it) }
        }
    }

    return element
}