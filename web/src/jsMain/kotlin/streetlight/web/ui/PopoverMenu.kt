package streetlight.web.ui

import kampfire.model.MutableTap
import kampfire.model.storeOf
import koala.css.Gap2Px
import koala.css.ModifierSet
import koala.css.modify
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

fun <T> ViewScope.popoverMenu(
    popoverId: Id,
    transform: (HTMLElement) -> T?,
    mod: ModifierSet? = null,
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

    element.addEventListener("toggle", { event ->
        val toggle = event.asDynamic()
        val invoker = toggle.source as? HTMLElement ?: return@addEventListener
        if (toggle.newState == "open") {
            currentInvoker = invoker
            state.set(transform(invoker) ?: defaultValue ?: error("popover menu content not found"))
        } else {
            state.set(null)
            if (invoker !== currentInvoker) {
                element.asDynamic().showPopover(json("source" to invoker))
            }
        }
    })
}