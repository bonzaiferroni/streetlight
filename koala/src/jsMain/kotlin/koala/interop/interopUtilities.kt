package koala.interop

import koala.dom.getElementOrNullById
import koala.dom.viewTransition
import koala.modifier.TransitioningRoot
import koala.modifier.modify
import koala.modifier.unmodify
import web.cssom.ClassName
import web.dom.document
import web.scroll.ScrollBehavior
import web.scroll.ScrollIntoViewOptions
import web.scroll.smooth
import web.storage.localStorage

/** The Kotlin functions Koala exposes to markup, added to `globalThis` at startup. */
val interopUtilities = listOf(
    KtFunction(KoalaFun.ScrollToId, ::scrollToId),
    KtFunction(KoalaFun.toggleRootModifier, ::toggleRootModifier),
    KtFunction(KoalaFun.toggleRootModifierWithTransition, ::toggleRootModifierWithTransition),
    KtFunction(KoalaFun.applyRootSwitch, ::applyRootSwitch),
    KtFunction(KoalaFun.applyRootSwitchWithTransition, ::applyRootSwitchWithTransition),
)

/** Scrolls smoothly to the element with [id]. */
fun scrollToId(id: String) = document.getElementOrNullById(id)?.scrollIntoView(ScrollIntoViewOptions(ScrollBehavior.smooth))

/** Toggles the class [mod] on the root element and stores whether it is on in `localStorage`. */
fun toggleRootModifier(mod: String) {
    val classes = document.documentElement.classList
    classes.toggle(ClassName(mod))
    localStorage.setItem(mod, classes.contains(ClassName(mod)).toString())
}

/** Sets whether the class [mod] is on the root element, and stores it in `localStorage`. */
fun setRootModifier(mod: String, isOn: Boolean) {
    document.documentElement.classList.toggle(ClassName(mod), isOn)
    localStorage.setItem(mod, isOn.toString())
}

/** Sets the root element's inline style to [cssText] and stores it in `localStorage` under [key]. */
fun applyRootStyle(key: String, cssText: String) {
    document.documentElement.style.cssText = cssText
    localStorage.setItem(key, cssText)
}

/** Sets the root setting [name] to [value] on the root element and in `localStorage`. */
fun applyRootSwitch(name: String, value: String) {
    document.documentElement.setAttribute(name, value)
    localStorage.setItem(name, value)
}

/** [toggleRootModifier] inside a view transition, with [TransitioningRoot] on the root until it is finished. */
fun toggleRootModifierWithTransition(mod: String) = transitionRoot { toggleRootModifier(mod) }

/** [applyRootSwitch] inside a view transition, with [TransitioningRoot] on the root until it is finished. */
fun applyRootSwitchWithTransition(name: String, value: String) = transitionRoot { applyRootSwitch(name, value) }

/** The root transitions under way, which keep [TransitioningRoot] on the root while any runs. */
private var rootTransitionCount = 0

/** Runs [update], a change to the root, inside a view transition with [TransitioningRoot] on the root until it is finished. */
private fun transitionRoot(update: () -> Unit) {
    val root = document.documentElement
    rootTransitionCount++
    root.modify(TransitioningRoot)
    document.viewTransition(update).finished.then {
        rootTransitionCount--
        if (rootTransitionCount == 0) root.unmodify(TransitioningRoot)
        null
    }
}