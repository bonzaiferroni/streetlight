@file:OptIn(ExperimentalWasmJsInterop::class)

package koala.interop

import js.array.asList
import koala.dom.viewTransition
import koala.layout.TabsStyle
import koala.layout.TransTabsStyle
import koala.modifier.getAttribute
import koala.modifier.isModified
import koala.modifier.modify
import koala.modifier.requireAttribute
import koala.modifier.setAttribute
import koala.modifier.unmodify
import web.dom.document
import web.events.CLICK
import web.events.Event
import web.events.addEventListener
import web.html.HTMLElement
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit

/**
 * Wires a trans tabs [container]: its buttons select a panel inside a view transition, the light sliding to the button
 * chosen, and the selection is restored from `localStorage` or the url when the container has a key for either.
 *
 * Does nothing for a container already initialized.
 */
fun initTransTabs(container: HTMLElement) {
    if (!container.isModified(TransTabsStyle.Initial)) return
    container.unmodify(TransTabsStyle.Initial)

    // only this container's own buttons and panels, not those of tabs nested in a panel
    val header = ":scope > ${TabsStyle.Header.selector}"
    val buttons = container.querySelectorAll("$header > ${TabsStyle.Button.selector}").asList()
    val panels = container.querySelectorAll(":scope > ${TransTabsStyle.Viewport.selector} > ${TransTabsStyle.Panel.selector}").asList()
    val light = container.querySelector("$header ${TransTabsStyle.Light.selector}")
    val viewport = container.querySelector(":scope > ${TransTabsStyle.Viewport.selector}")
    val storageKey = container.getAttribute(TabsStyle.StorageKey)
    val param = container.getAttribute(TabsStyle.ParamKey)
    var shownIndex = container.requireAttribute(TabsStyle.Index)
    // the index last asked for, ahead of shownIndex while a transition waits to run
    var requestedIndex = shownIndex

    fun show(index: Int) {
        panels[shownIndex].unmodify(TabsStyle.IsActive)
        buttons[shownIndex].unmodify(TabsStyle.IsActive)
        shownIndex = index
        panels[index].modify(TabsStyle.IsActive)
        buttons[index].modify(TabsStyle.IsActive)
        light?.let { buttons[index].prepend(it) }
    }

    fun select(index: Int) {
        if (index == requestedIndex || index !in panels.indices) return
        // set before the transition starts, so both captures carry the direction
        val isForward = index > requestedIndex
        viewport?.modify(if (isForward) TransTabsStyle.Forward else TransTabsStyle.Backward)
            ?.unmodify(if (isForward) TransTabsStyle.Backward else TransTabsStyle.Forward)
        requestedIndex = index
        storageKey?.let { writeStorageIndex(it, index) }
        param?.let { writeParamIndex(it, index) }
        val transition = document.viewTransition { show(index) }
        // the direction comes off once this change is done, so a later transition elsewhere does not slide the panels;
        // a newer selection keeps its own
        transition.finished.then {
            if (requestedIndex == index) {
                viewport?.unmodify(TransTabsStyle.Forward)?.unmodify(TransTabsStyle.Backward)
            }
            null
        }
    }

    buttons.forEachIndexed { index, button ->
        button.addEventListener(Event.CLICK, {
            container.setAttribute(TabsStyle.Index.to(index))
        })
    }

    val observer = MutationObserver { _, _ -> select(container.requireAttribute(TabsStyle.Index)) }
    observer.observe(container, MutationObserverInit(
        attributes = true,
        attributeFilter = arrayOf(TabsStyle.Index.name)
    ))

    // a selection restored on load is shown at once, without a transition; the url outranks storage, as in initTabs
    val restoredIndex = (param?.let { readParamIndex(it) } ?: storageKey?.let { readStorageIndex(it) })
        ?.takeIf { it in panels.indices && it != shownIndex }
    restoredIndex?.let { index ->
        requestedIndex = index
        show(index)
        container.setAttribute(TabsStyle.Index.to(index))
    }
}

/** Wires every trans tabs container under [parent] that is not yet initialized, with [initTransTabs]. */
fun findAndInitTransTabs(parent: HTMLElement) {
    parent.querySelectorAll(TransTabsStyle.Initial.selector).asList().forEach { initTransTabs(it as HTMLElement) }
}
