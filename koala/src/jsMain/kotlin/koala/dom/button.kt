package koala.dom

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.html.BUTTON
import kotlinx.html.js.button
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.Event

fun DOMContext.button(
    text: String,
    modifiers: ModifierSet? = null,
    onClick: ((Event) -> Unit)? = null,
    block: (BUTTON.() -> Unit)? = null,
): HTMLButtonElement {
    val element = button {
        applyModifiers(modify(ElementClass.button, modifiers))
        +text

        block?.invoke(this)
    }

    onClick?.let {
        element.addEventListener("click", it)
    }

    return element
}