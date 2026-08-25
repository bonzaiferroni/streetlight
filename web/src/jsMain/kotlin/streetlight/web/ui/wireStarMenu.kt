package streetlight.web.ui

import kampfire.api.Username
import kampfire.api.toUsername
import koala.css.OpacityHalf
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.getAttribute
import koala.dom.popoverRaw
import koala.dom.popover
import koala.dom.popoverCard
import koala.dom.popoverOption
import koala.dom.textBlock
import koala.html.Attribute
import koala.html.Id
import koala.model.MutableTap
import koala.model.storeOf
import org.w3c.dom.HTMLElement
import streetlight.model.ui.StarRoute
import kotlin.js.json

fun ViewScope.wireStarMenu() {
    popoverMenu(
        popoverId = StarMenu.PopoverId,
        transform = {
            it.getAttribute(Attribute.Username)
        }
    ) { username ->
        column {
            popoverOption(StarRoute(username))
        }
    }
}

fun <T> ViewScope.popoverMenu(
    popoverId: Id,
    transform: (HTMLElement) -> T?,
    state: MutableTap<T?> = storeOf(null),
    content: ViewScope.(T) -> Unit
) {
    var currentInvoker: HTMLElement? = null

    val element = popoverRaw(popoverId) {
        flowBlock(state) { value ->
            if (value == null) return@flowBlock
            popoverCard {
                content(value)
            }
        }
    }

    element.addEventListener("toggle", { event ->
        val toggle = event.asDynamic()
        val invoker = toggle.source as? HTMLElement ?: return@addEventListener
        if (toggle.newState == "open") {
            currentInvoker = invoker
            state.set(transform(invoker))
        } else if (invoker !== currentInvoker) {
            element.asDynamic().showPopover(json("source" to invoker))
        }
    })
}