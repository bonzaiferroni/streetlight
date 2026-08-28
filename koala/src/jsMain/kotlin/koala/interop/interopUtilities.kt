package koala.interop

import koala.dom.getElementOrNullById
import koala.dom.viewTransition
import web.cssom.ClassName
import web.dom.document
import web.html.HTMLElement
import web.scroll.ScrollBehavior
import web.scroll.ScrollIntoViewOptions
import web.scroll.smooth
import web.storage.localStorage

val interopUtilities = listOf(
    KtFunction(KoalaFun.ScrollToId, ::scrollToId),
    KtFunction(KoalaFun.ToggleAncestor, ::toggleAncestor),
    KtFunction(KoalaFun.toggleRootModifier, ::toggleRootModifier),
    KtFunction(KoalaFun.toggleRootModifierWithTransition, ::toggleRootModifierWithTransition),
)

fun scrollToId(id: String) = document.getElementOrNullById(id)?.scrollIntoView(ScrollIntoViewOptions(ScrollBehavior.smooth))

fun toggleAncestor(element: HTMLElement, ancestorClass: String, toggleClass: String) {
    var ancestor = element.parentElement
    while (ancestor != null) {
        if (ancestor.classList.contains(ClassName(ancestorClass))) {
            document.viewTransition {
                (ancestor as HTMLElement).classList.toggle(ClassName(toggleClass))
            }
            break
        }
        ancestor = ancestor.parentElement
    }
}

fun toggleRootModifier(mod: String) {
    val classes = document.documentElement.classList
    classes.toggle(ClassName(mod))
    localStorage.setItem(mod, classes.contains(ClassName(mod)).toString())
}

fun toggleRootModifierWithTransition(mod: String) {
    document.startViewTransition {
        toggleRootModifier(mod)
        null
    }
}