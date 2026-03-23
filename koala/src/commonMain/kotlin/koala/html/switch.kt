package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.span

fun FlowContent.switch(
    label: String,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    initialOn: Boolean = false,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        configureSwitch(label, modifiers, id, initialOn, block)
    }
}

fun DIV.configureSwitch(
    label: String,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    initialOn: Boolean = false,
    block: (DIV.() -> Unit)? = null,
) {
    applyModifiers(modify(ElementClass.switch, modifiers))
    applyId(id)
    attributes["role"] = "switch"
    attributes["aria-checked"] = initialOn.toString()
    attributes[Attribute.isOn] = initialOn.toString()

    // ghost text defines the inner pill width; outer padding makes the “constraints” larger
    span("switch__ghost") { +label }
    span("switch__pill") { +label }

    block?.invoke(this)
}