package koala.dom

import koala.Svg
import koala.css.Aspect1
import koala.css.Confirm
import koala.css.Danger
import koala.css.Height3
import koala.css.ModifierSet
import koala.css.Secondary
import koala.css.addModifiers
import koala.css.modify
import koala.html.BtnKey
import koala.html.configureButton
import koala.html.configureElementButton
import koala.html.configureSvgButton
import koala.html.span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.BUTTON
import kotlinx.html.js.button
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.Event

fun DOMContext.button(
    text: String,
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    flair: String? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureButton(text, modifiers, flair, block)
    }

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
    )

    return element
}

fun DOMContext.button(
    svg: Svg,
    modifiers: ModifierSet? = modify(Aspect1, Height3),
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureSvgButton(svg, modifiers, block)
    }

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
    )

    return element
}

fun DOMContext.button(
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureElementButton(modifiers, block)
    }

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
    )

    return element
}

fun DOMContext.dangerButton(
    text: String,
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
    flair: String? = null,
    block: BUTTON.() -> Unit = {},
) {
    var isConfirm = false

    val element = button {
        configureButton(text, modify(Secondary, modifiers), flair, block)
    }

    element.onClick {
        if (!isConfirm) {
            isConfirm = true
            element.unmodify(Secondary)
            element.modify(Danger)
            element.textContent = "Confirm"
        } else {
            onClick?.invoke()
        }
    }
}

private fun configureButtonEvents(
    element: HTMLButtonElement,
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
) {
    onClickEvent?.let {
        element.addEventListener("click", it)
    }

    onClick?.let {
        element.addEventListener("click", { it() })
    }
}

fun RenderContext.configureEnabledFlow(
    element: HTMLButtonElement,
    flow: Flow<Boolean>? = null,
) {
    flow?.let {
        renderScope.launch {
            it.collect { isEnabled ->
                element.disabled = !isEnabled
            }
        }
    }
}