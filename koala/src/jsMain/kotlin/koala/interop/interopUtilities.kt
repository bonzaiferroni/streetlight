package koala.interop

import koala.dom.startViewTransition
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLElement
import org.w3c.dom.SMOOTH
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollOptions

val interopUtilities = listOf(
    KtFunction(KoalaInlineJs.ScrollToId, ::scrollToId),
    KtFunction(KoalaInlineJs.ToggleAncestor, ::toggleAncestor),
    KtFunction(KoalaInlineJs.toggleRootModifier, ::toggleRootModifier),
    KtFunction(KoalaInlineJs.toggleRootModifierWithTransition, ::toggleRootModifierWithTransition),
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

fun toggleRootModifier(mod: String) {
    val classes = document.documentElement!!.classList
    classes.toggle(mod)
    localStorage.setItem(mod, classes.contains(mod).toString())
}

fun toggleRootModifierWithTransition(mod: String) {
    document.startViewTransition {
        toggleRootModifier(mod)
    }
}