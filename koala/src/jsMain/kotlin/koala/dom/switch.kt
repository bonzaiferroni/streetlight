package koala.dom

import koala.core.initSwitch
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.html.ElementEvent
import koala.html.Id
import koala.html.Queryable
import koala.html.configureSwitch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun RenderContext.switch(
    label: String,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    initialOn: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    bindFlow: Flow<Boolean>? = null,
    block: (DIV.() -> Unit)? = null,
): HTMLDivElement {
    val element = div {
        configureSwitch(label, modifiers, id, initialOn, block)
    }

    initSwitch(element)
    wireSwitch(
        element = element,
        initialOn = initialOn,
        onToggle = onToggle,
        bindFlow = bindFlow,
    )

    return element
}

fun RenderContext.wireSwitch(
    element: HTMLElement,
    initialOn: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    bindFlow: Flow<Boolean>? = null,
) {
    var isOn = initialOn

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
}

fun RenderContext.queryAndWireSwitch(
    ancestor: HTMLElement,
    queryable: Queryable = ElementClass.switch,
    initialOn: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    bindFlow: Flow<Boolean>? = null,
) {
    val element = ancestor.querySelector(queryable)
        ?: error("switch not found with selector: ${queryable.selector}")
    wireSwitch(element, initialOn, onToggle, bindFlow)
}