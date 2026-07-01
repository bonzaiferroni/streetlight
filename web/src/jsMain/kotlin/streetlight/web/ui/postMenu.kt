package streetlight.web.ui

import kampfire.api.Slug
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
import koala.html.btn
import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import streetlight.web.PostUpdateRoute

fun AppScope.initPostMenu(shellBase: HTMLElement) {
    val targets = shellBase.queryAttributeAll(PostMenu.Slug)

    var activeSlug: Slug? = null

    targets.forEach { (element, slug) ->
        val username = element.getAttribute(PostMenu.Username)
        var shouldSkip = false

        element.addEventListener("pointerdown", {
            shouldSkip = activeSlug == slug && cachedMenuElement?.matches(":popover-open") == true
        })

        element.onClick {
            if (shouldSkip) {
                shouldSkip = false
                return@onClick
            }
            cachedMenuElement?.hidePopover()
            activeSlug = slug
            callPostMenu(slug, username)
        }
    }
}

private var cachedMenuElement: HTMLElement? = null

fun AppScope.callPostMenu(slug: Slug, username: Username?) {
    val menuElement = cachedMenuElement ?: document.body!!.append {
        popover(PostMenu.MenuId, null, modify(Magic, SlideUp))
    }.first().also { cachedMenuElement = it }

    val isUser = session.stateNow.star?.username == username

    menuElement.clear()
    menuElement.append {
        card(modify(BlurBackdrop, BorderRadius3)) {
            column {
                if (isUser) {
                    btn("edit", PostUpdateRoute(slug), modify(Secondary))
                } else {
                    button("report", modify(Secondary))
                }
                dangerButton("remove", onClick = {
                    parentScope.launch {
                        api.removePost(slug).handleOutcome(toaster::toast) {
                            portal.refresh()
                        }
                    }
                })
            }
        }
    }
}