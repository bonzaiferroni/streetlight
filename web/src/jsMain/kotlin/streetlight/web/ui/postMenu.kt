package streetlight.web.ui

import kampfire.api.Slug
import kampfire.model.handleResponse
import koala.css.BlurBackdrop
import koala.css.BorderRadius3
import koala.css.Magic
import koala.css.Secondary
import koala.css.SlideUp
import koala.css.modify
import koala.dom.AppFacade
import koala.dom.AppScope
import koala.dom.EffectScope
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.dangerButton
import koala.dom.hidePopover
import koala.dom.isPopoverOpen
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
    val targets = shellBase.queryAttributeAll(PostMenu.Attribute)
    targets.forEach { (element, slug) ->
        element.onClick {
            cachedMenuElement?.hidePopover()
            callPostMenu(slug)
        }
    }
}

private var cachedMenuElement: HTMLElement? = null

fun AppScope.callPostMenu(slug: Slug) {
    val menuElement = cachedMenuElement ?: document.body!!.append {
        popover(PostMenu.MenuId, null, modify(Magic, SlideUp))
    }.first().also { cachedMenuElement = it }

    menuElement.clear()
    menuElement.append {
        card(modify(BlurBackdrop, BorderRadius3)) {
            column {
                btn("edit", PostUpdateRoute(slug), modify(Secondary))
                button("report", modify(Secondary))
                dangerButton("remove", onClick = {
                    parentScope.launch {
                        api.removePost(slug).handleResponse(toaster::toast) {
                            portal.refresh()
                        }
                    }
                })
            }
        }
    }
}