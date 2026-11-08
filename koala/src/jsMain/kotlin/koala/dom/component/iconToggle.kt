package koala.dom.component

import kampfire.model.Tap
import kampfire.model.tapOf
import koala.Svg
import koala.component.IconStyle
import koala.dom.ViewScope
import koala.modifier.*
import kotlinx.coroutines.launch
import web.html.HTMLButtonElement

/**
 * A button labeled [label] showing [onSvg] while [isOnState] is on and [offSvg] while off, calling [onClick] when
 * clicked. It reads to assistive technology as a toggle, pressed while on.
 */
fun ViewScope.iconToggle(
    label: String,
    isOnState: Tap<Boolean>,
    onSvg: Svg,
    offSvg: Svg,
    onClick: () -> Unit,
    mod: Modifier? = null,
    iconMod: Modifier? = IconStyle.DefaultMod,
): HTMLButtonElement {
    val element = button(onClick, modify(Attribute.AriaLabel.to(label), mod)) {
        icon(isOnState.tapOf { if (it) onSvg else offSvg }, iconMod)
    }
    contentScope.launch {
        isOnState.flow.collect { element.setAttribute("aria-pressed", it.toString()) }
    }
    element.setAttribute("aria-pressed", isOnState.now.toString())
    return element
}
