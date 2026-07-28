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
import koala.model.Field
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

@Deprecated("use field")
fun ViewScope.configureEnabledFlow(
    element: HTMLButtonElement,
    flow: Flow<Boolean>? = null,
) {
    flow?.let {
        contentScope.launch {
            it.collect { isEnabled ->
                element.disabled = !isEnabled
            }
        }
    }
}

fun ViewScope.configureEnabledFlow(
    element: HTMLButtonElement,
    field: Field<Boolean>,
) {
    element.disabled = field.now
    contentScope.launch {
        field.flow.collect { isEnabled ->
            element.disabled = !isEnabled
        }
    }
}