package koala.dom

import koala.core.initSwitch
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import koala.html.ElementEvent
import koala.html.configureSwitch
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
        configureSwitch(label, modifiers, initialOn, block)
    }

    fun setOn(value: Boolean) {
        if (value == isOn) return
        isOn = value
        onToggle?.invoke(value)
        element.sendCustomEvent(ElementEvent.onToggle, value)
    }

    element.onCustomEvent(ElementEvent.onToggle) {
        setOn(it)
    }

    bindFlow?.let { flow ->
        renderScope.launch {
            flow.collect {
                setOn(it)
            }
        }
    }

    initSwitch(element)

    return element
}