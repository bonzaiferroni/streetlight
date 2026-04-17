package koala.dom

import koala.Svg
import koala.css.Aspect1
import koala.css.Height3
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.css.modify
import koala.html.BtnKey
import koala.html.configureElementButton
import koala.html.configureSvgButton
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
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        addModifiers(modify(BtnKey.Class, modifiers))
//        setId(id)
//        setStyle(styles)
        block()
        +text
    }

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
        bindIsEnabled = bindIsEnabled
    )

    return element
}

fun RenderContext.button(
    svg: Svg,
    modifiers: ModifierSet? = modify(Aspect1, Height3),
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    bindIsEnabled: Flow<Boolean>? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureSvgButton(svg, modifiers, block)
    }

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
        bindIsEnabled = bindIsEnabled
    )

    return element
}

fun RenderContext.button(
    modifiers: ModifierSet? = null,
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    bindIsEnabled: Flow<Boolean>? = null,
    block: BUTTON.() -> Unit = {},
): HTMLButtonElement {
    val element = button {
        configureElementButton(modifiers, block)
    }

    configureButtonEvents(
        element = element,
        onClick = onClick,
        onClickEvent = onClickEvent,
        bindIsEnabled = bindIsEnabled
    )

    return element
}

fun RenderContext.configureButtonEvents(
    element: HTMLButtonElement,
    onClick: (() -> Unit)? = null,
    onClickEvent: ((Event) -> Unit)? = null,
    bindIsEnabled: Flow<Boolean>? = null,
) {
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
}



//fun <T> WireContext<T>.button(
//    text: (T) -> String,
//    modifiers: ModifierSet? = null,
//    onClick: (() -> Unit)? = null,
//    onClickEvent: ((Event) -> Unit)? = null,
//    isEnabled: ((T) -> Boolean)? = null,
//    block: BUTTON.() -> Unit = {},
//) {
//    val element = button(
//        text = text(state.now),
//        modifiers = modifiers,
//        onClick = onClick,
//        onClickEvent = onClickEvent,
//        isEnabled = isEnabled?.let { isEnabled -> state.flow.mapDistinct { isEnabled(it) }},
//        block = block
//    )
////    val element = button(
////        text = text(state.now),
////        modifiers = modifiers,
////        onClick = onClick,
////        onClickEvent = onClickEvent,
////        bindIsEnabled = isEnabled?.let { isEnabled -> state.flow.mapDistinct { isEnabled(it) }},
////        block = block
////    )
//
//    renderScope.launch {
//        state.flow.collect {
//            element.textContent = text(it)
//        }
//    }
//}