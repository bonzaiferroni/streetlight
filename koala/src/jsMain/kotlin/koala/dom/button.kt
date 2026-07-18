package koala.dom

import koala.Svg
import koala.css.Aspect1
import koala.css.Danger
import koala.css.Height3
import koala.css.ModifierSet
import koala.css.Zen
import koala.css.modify
import koala.html.configureButton
import koala.html.configureElementButton
import koala.html.configureSvgButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.BUTTON
import kotlinx.html.js.button
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.Event

fun TagScope.button(
    text: String,
    onClick: (() -> Unit)? = null,
    mod: ModifierSet? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    flair: String? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureButton(text, mod, flair, block)
    }

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
    )

    return element
}

fun TagScope.button(
    svg: Svg,
    onClick: (() -> Unit)? = null,
    mod: ModifierSet? = modify(Aspect1, Height3),
    onClickEvent: ((Event) -> Unit)? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureSvgButton(svg, mod, block)
    }

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
    )

    return element
}

fun TagScope.button(
    onClick: (() -> Unit)? = null,
    mod: ModifierSet? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureElementButton(mod, block)
    }

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
    )

    return element
}

fun TagScope.dangerButton(
    text: String,
    onClick: (() -> Unit)? = null,
    mod: ModifierSet? = null,
    flair: String? = null,
    block: BUTTON.() -> Unit = {},
) {
    var isConfirm = false

    val element = button {
        configureButton(text, modify(Zen, mod), flair, block)
    }

    element.onClick {
        if (!isConfirm) {
            isConfirm = true
            element.unmodify(Zen)
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

fun ViewScope.configureEnabledFlow(
    element: HTMLButtonElement,
    flow: Flow<Boolean>? = null,
) {
    flow?.let {
        scope.launch {
            it.collect { isEnabled ->
                element.disabled = !isEnabled
            }
        }
    }
}

fun HTMLButtonElement.enabledFlow(
    scope: CoroutineScope,
    flow: Flow<Boolean>
) {
    scope.launch {
        flow.collect { isEnabled ->
            disabled = !isEnabled
        }
    }
}