package streetlight.web.ui

import kampfire.api.Username
import kampfire.model.handleOutcome
import koala.css.BlurBackdrop
import koala.css.BorderRadius3
import koala.css.Magic
import koala.css.Secondary
import koala.css.SlideUp
import koala.css.modify
import koala.dom.AppScope
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.dangerButton
import koala.dom.getAttribute
import koala.dom.hidePopover
import koala.dom.onClick
import koala.dom.popover
import koala.dom.queryAttributeAll
import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import streetlight.model.data.PostId

fun AppScope.initPostMenu(shellBase: HTMLElement) {
    val targets = shellBase.queryAttributeAll(PostMenu.PostId)

    var activeId: PostId? = null

    targets.forEach { (element, postId) ->
        val username = element.getAttribute(PostMenu.Username)
        var shouldSkip = false

        element.addEventListener("pointerdown", {
            shouldSkip = activeId == postId && cachedMenuElement?.matches(":popover-open") == true
        })

        element.onClick {
            if (shouldSkip) {
                shouldSkip = false
                return@onClick
            }
            cachedMenuElement?.hidePopover()
            activeId = postId
            callPostMenu(postId, username)
        }
    }
}

private var cachedMenuElement: HTMLElement? = null

fun AppScope.callPostMenu(postId: PostId, username: Username?) {
    val menuElement = cachedMenuElement ?: document.body!!.append {
        popover(PostMenu.MenuId, null, modify(Magic, SlideUp))
    }.first().also { cachedMenuElement = it }

    val isUser = session.stateNow.star?.username == username

    menuElement.clear()
    menuElement.append {
        card(modify(BlurBackdrop, BorderRadius3)) {
            column {
                if (isUser) {
                    // td: figure out what edit options to provide here
                    // do we edit the post or the target record?
                    // btn("edit", MediaUpdateRoute(postId), modify(Secondary))
                } else {
                    button("report", modify(Secondary))
                }
                dangerButton("remove", onClick = {
                    parentScope.launch {
                        api.removePost(postId).handleOutcome(toaster::toast) {
                            portal.refresh()
                        }
                    }
                })
            }
        }
    }
}