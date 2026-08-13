package koala.core

import koala.css.KoalaFun
import koala.dom.startViewTransition
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.SMOOTH
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollOptions

val globalFunCore = listOf(
    KoalaFun.ScrollToId to ::scrollToId,
)

fun scrollToId(id: String) = document.getElementById(id)?.scrollIntoView(ScrollOptions(ScrollBehavior.SMOOTH))

fun toggleAncestor(element: HTMLElement, ancestorClass: String, toggleClass: String) {
    var ancestor = element.parentElement
    while (ancestor != null) {
        if (ancestor.classList.contains(ancestorClass)) {
            document.startViewTransition {
                (ancestor as HTMLElement).classList.toggle(toggleClass)
            }
            break
        }
        ancestor = ancestor.parentElement
    }
}