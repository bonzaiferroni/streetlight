package koala.dom

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.BUTTON
import kotlinx.html.js.button
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.Event

fun RenderContext.button(
    text: String,
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    bindIsEnabled: Flow<Boolean>? = null,
    block: (BUTTON.() -> Unit)? = null,
): HTMLButtonElement {
    val element = button {
        applyModifiers(modify(ElementClass.button, modifiers))
        +text

        block?.invoke(this)
    }

    onClickEvent?.let {
        element.addEventListener("click", it)
    }

    onClick?.let {
        element.addEventListener("click", { it() })
    }

    bindIsEnabled?.let {
        renderScope.launch {
            it.collect { isEnabled ->
                element.disabled = !isEnabled
            }
        }
    }

    return element
}