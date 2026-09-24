package streetlight.web.ui

import kampfire.model.MutableTap
import kampfire.model.storeOf
import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.flowBlock
import koala.dom.popoverCard
import koala.dom.popoverRaw
import koala.html.Id
import web.html.HTMLElement
import kotlin.js.json

object PopoverMenuMod {
    val Column = modify(Gap2Px)
}

/**
 * A popover shared by every button that targets [popoverId]. It anchors to the button that opened it and shows
 * [content] for the value [transform] reads from that button.
 *
 * A click on another button while open moves the popover to it.
 */
fun <T> ViewScope.popoverMenu(
    popoverId: Id,
    transform: (HTMLElement) -> T?,
    mod: Modifier? = null,
    state: MutableTap<T?> = storeOf(null),
    defaultValue: T? = null,
    content: ViewScope.(T) -> Unit
) {
    var currentInvoker: HTMLElement? = null

    val element = popoverRaw(popoverId) {
        flowBlock(state) { value ->
            if (value == null) return@flowBlock
            popoverCard(mod) {
                content(value)
            }
        }
    }

    val anchor = popoverId.toPositionAnchor()

    element.addEventListener("toggle", { event ->
        val toggle = event.asDynamic()
        val invoker = toggle.source as? HTMLElement ?: return@addEventListener
        if (toggle.newState == "open") {
            currentInvoker?.removeStyle(Css.AnchorName)
            currentInvoker = invoker
            invoker.modify(Css.AnchorName.of(anchor))
            state.set(transform(invoker) ?: defaultValue ?: error("popover menu content not found"))
        } else {
            state.set(null)
            if (invoker !== currentInvoker) {
                element.asDynamic().showPopover(json("source" to invoker))
            }
        }
    })
}