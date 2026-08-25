package streetlight.web.ui

import kampfire.api.Username
import kampfire.api.toUsername
import koala.css.DisplayNone
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.getAttribute
import koala.dom.modify
import koala.dom.popoverCard
import koala.dom.popoverOption
import koala.dom.textBlock
import koala.dom.unmodify
import koala.html.Attribute
import koala.model.storeOf
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import streetlight.model.ui.StarRoute
import kotlin.js.json

fun ViewScope.wireStarMenu() {
    val usernameState = storeOf<Username?>(null)
    var currentInvoker: HTMLElement? = null

    val element = popoverCard(StarMenu.PopoverId) {
        flowBlock(usernameState) { username ->
            when (username) {
                null -> textBlock("null")
                else -> column {
                    popoverOption(StarRoute(username))
                }
            }
        }
    }

    element.addEventListener("toggle", { event ->
        val toggle = event.asDynamic()
        val invoker = toggle.source as? HTMLElement
        if (toggle.newState == "open") {
            currentInvoker = invoker
            usernameState.set(invoker?.getAttribute(Attribute.Username)?.toUsername())
        } else if (invoker != null && invoker !== currentInvoker) {
            element.asDynamic().showPopover(json("source" to invoker))
        }
    })
}