@file:OptIn(ExperimentalWasmJsInterop::class)

package koala.interop

import koala.modifier.className
import koala.modifier.isModified
import koala.dom.querySelector
import koala.dom.querySelectorAll
import koala.modifier.requireAttribute
import koala.modifier.setAttribute
import koala.modifier.unmodify
import koala.layout.TabsStyle
import koala.modifier.getAttribute
import koala.model.Portal
import koala.model.QUERY_PARAM_CHANGE
import koala.model.QueryParamChange
import web.animations.requestAnimationFrame
import web.cssom.TRANSITION_END
import web.cssom.TransitionEvent
import web.events.CLICK
import web.events.CustomEvent
import web.events.CustomEventInit
import web.events.Event
import web.events.addEventListener
import web.html.HTMLElement
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit
import web.storage.localStorage
import web.url.URL
import web.window.window

/**
 * Wires a server-rendered tabs [container]: its buttons select a panel, the viewport animates to the panel's
 * height, and the selection is restored from `localStorage` when the container has an id.
 *
 * Does nothing for a container already initialized.
 */
fun initTabs(container: HTMLElement) {
    if (!container.isModified(TabsStyle.Initial)) return
    container.unmodify(TabsStyle.Initial)

    val viewport = container.querySelector(TabsStyle.Viewport) ?: error("no viewport found")
    val buttons = container.querySelectorAll(TabsStyle.Button)
    val panels = container.querySelectorAll(TabsStyle.Panel)
    val storageKey = container.id.toString().takeIf { it.isNotEmpty() }?.let { "${STORAGE_KEY_BASE}.$it" }
    val param = container.getAttribute(TabsStyle.Param)
    var currentIndex = container.requireAttribute(TabsStyle.Index)

    fun changeTab(index: Int) {
        if (currentIndex == index || index !in panels.indices) return

        val startHeight = viewport.offsetHeight
        val endHeight = (panels[index] as HTMLElement).offsetHeight

        val animatesHeight = startHeight != endHeight

        if (animatesHeight) {
            viewport.style.setProperty("height", "${startHeight}px")
            viewport.offsetHeight // a load bearing property reference
        }

        panels[currentIndex].classList.remove(TabsStyle.IsActive.className)
        buttons[currentIndex].classList.remove(TabsStyle.IsActive.className)
        currentIndex = index

        panels[index].classList.add(TabsStyle.IsActive.className)
        buttons[index].classList.add(TabsStyle.IsActive.className)

        if (animatesHeight) viewport.style.setProperty("height", "${endHeight}px")
    }

    buttons.forEachIndexed { index, button ->
        button.addEventListener(Event.CLICK, {
            container.setAttribute(TabsStyle.Index.to(index))
        })
    }

    viewport.addEventListener(TransitionEvent.TRANSITION_END, { event ->
        if (event.target == viewport && event.propertyName == "height") {
            viewport.style.setProperty("height", "auto")
        }
    })

    val observer = MutationObserver { _, _ ->
        requestAnimationFrame {
            val index = container.requireAttribute(TabsStyle.Index)
            storageKey?.let {
                localStorage.setItem(it, index.toString())
            }
            param?.let {
                writeParamIndex(it, index)
            }
            changeTab(index)
        }
    }
    observer.observe(container, MutationObserverInit(
        attributes = true,
        attributeFilter = arrayOf(TabsStyle.Index.name)
    ))

    storageKey?.let { key ->
        localStorage.getItem(key)?.toIntOrNull()?.takeIf { it in panels.indices }?.let { index ->
            container.setAttribute(TabsStyle.Index.to(index))
        }
    }
    param?.let { key ->
        readParamIndex(key)?.takeIf { it in panels.indices }?.let { index ->
            container.setAttribute(TabsStyle.Index.to(index))
        }
    }
}

/** Wires every tabs container under [parent] that is not yet initialized, with [initTabs]. */
fun findAndInitTabs(parent: HTMLElement) {
    parent.querySelectorAll(TabsStyle.Initial).forEach { initTabs(it as HTMLElement) }
}

/** Reads the tab index held by the query [param] of the current url, or null when absent or not an index. */
private fun readParamIndex(param: String): Int? =
    URL(window.location.href).searchParams.get(param.toJsString())?.toString()?.toIntOrNull()

/** Asks [Portal] to set the query [param] of the current route to [index]. */
private fun writeParamIndex(param: String, index: Int) {
    val change = QueryParamChange(key = param, value = index.toString())
    window.dispatchEvent(CustomEvent(QUERY_PARAM_CHANGE, CustomEventInit(detail = change)))
}

private const val STORAGE_KEY_BASE = "streetlight.tabValues"