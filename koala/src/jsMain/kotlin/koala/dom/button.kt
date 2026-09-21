package koala.dom

import koala.Svg
import koala.html.IconStyle
import koala.html.configureButton
import koala.html.configureElementButton
import koala.html.configureSvgButton
import kampfire.model.Tap
import kampfire.model.Url
import koala.html.BtnStyle
import koala.modifier.Modifier
import koala.modifier.modify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.A
import kotlinx.html.BUTTON
import kotlinx.html.js.button
import web.events.Event
import web.events.addEventListener
import web.html.HTMLButtonElement
import web.pointer.CLICK
import web.pointer.PointerEvent

fun AppendScope.button(
    text: String,
    onClick: (() -> Unit)? = null,
    mod: Modifier? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    flair: String? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureButton(text, mod, flair, block)
    }.asWeb()

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
    )

    return element
}

fun AppendScope.button(
    svg: Svg,
    onClick: (() -> Unit)? = null,
    mod: Modifier = IconStyle.DefaultMod,
    onClickEvent: ((Event) -> Unit)? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureSvgButton(svg, mod, block)
    }.asWeb()

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
    )

    return element
}

fun AppendScope.button(
    onClick: (() -> Unit)? = null,
    mod: Modifier? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureElementButton(mod, block)
    }.asWeb()

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
    )

    return element
}

fun AppendScope.button(
    label: String,
    url: Url,
    mod: Modifier? = null,
    config: A.() -> Unit = { }
) = navigation(url.value, modify(mod, BtnStyle.Class)) {
    config()
    span {
        +label
    }
}

private fun configureButtonEvents(
    element: HTMLButtonElement,
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
) {
    onClickEvent?.let {
        element.addEventListener(PointerEvent.CLICK, it)
    }

    onClick?.let {
        element.addEventListener(PointerEvent.CLICK, { it() })
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
    tap: Tap<Boolean>,
) {
    element.disabled = tap.now
    contentScope.launch {
        tap.flow.collect { isEnabled ->
            element.disabled = !isEnabled
        }
    }
}