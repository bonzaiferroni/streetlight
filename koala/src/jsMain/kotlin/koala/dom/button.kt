package koala.dom

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.StyleSet
import koala.css.addModifiers
import koala.css.setStyle
import koala.css.modify
import koala.html.BtnKey
import koala.html.Id
import koala.html.setId
import koala.model.mapDistinct
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
    id: Id? = null,
    styles: StyleSet? = null,
    block: (BUTTON.() -> Unit)? = null,
): HTMLButtonElement {
    val element = button {
        addModifiers(modify(BtnKey.Class, modifiers))
        setId(id)
        setStyle(styles)
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

fun <T> WireContext<T>.button(
    text: (T) -> String,
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    isEnabled: ((T) -> Boolean)? = null,
    block: (BUTTON.() -> Unit)? = null,
) {
    val element = button(
        text = text(state.now),
        modifiers = modifiers,
        onClick = onClick,
        onClickEvent = onClickEvent,
        bindIsEnabled = isEnabled?.let { isEnabled -> state.flow.mapDistinct { isEnabled(it) }},
        block = block
    )

    renderScope.launch {
        state.flow.collect {
            element.textContent = text(it)
        }
    }
}