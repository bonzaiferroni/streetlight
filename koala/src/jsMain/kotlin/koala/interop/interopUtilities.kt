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

/** The Kotlin functions Koala exposes to markup, added to `globalThis` at startup. */
val interopUtilities = listOf(
    KtFunction(KoalaFun.ScrollToId, ::scrollToId),
    KtFunction(KoalaFun.ToggleAncestor, ::toggleAncestor),
    KtFunction(KoalaFun.toggleRootModifier, ::toggleRootModifier),
    KtFunction(KoalaFun.toggleRootModifierWithTransition, ::toggleRootModifierWithTransition),
    KtFunction(KoalaFun.applyRootSwitch, ::applyRootSwitch),
)

/** Scrolls smoothly to the element with [id]. */
fun scrollToId(id: String) = document.getElementOrNullById(id)?.scrollIntoView(ScrollIntoViewOptions(ScrollBehavior.smooth))

/** Toggles [toggleClass] on the nearest ancestor of [element] with [ancestorClass]. */
fun toggleAncestor(element: HTMLElement, ancestorClass: String, toggleClass: String) {
    val ancestor = element.closest(".$ancestorClass") as? HTMLElement ?: return
    ancestor.classList.toggle(ClassName(toggleClass))
}

/** Toggles the class [mod] on the root element and stores whether it is on in `localStorage`. */
fun toggleRootModifier(mod: String) {
    val classes = document.documentElement.classList
    classes.toggle(ClassName(mod))
    localStorage.setItem(mod, classes.contains(ClassName(mod)).toString())
}

/** Sets the root setting [name] to [value] on the root element and in `localStorage`. */
fun applyRootSwitch(name: String, value: String) {
    document.documentElement.setAttribute(name, value)
    localStorage.setItem(name, value)
}

/** [toggleRootModifier] inside a view transition. */
fun toggleRootModifierWithTransition(mod: String) {
    document.startViewTransition {
        toggleRootModifier(mod)
        null
    }
}