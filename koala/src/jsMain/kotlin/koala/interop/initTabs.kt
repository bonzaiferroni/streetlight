package koala.interop

import koala.dom.className
import koala.dom.isModified
import koala.dom.modify
import koala.dom.querySelector
import koala.dom.querySelectorAll
import koala.dom.requireAttribute
import koala.dom.setAttribute
import koala.dom.unmodify
import koala.html.TabsStyle
import web.animations.requestAnimationFrame
import web.cssom.TRANSITION_END
import web.cssom.TransitionEvent
import web.events.CLICK
import web.events.Event
import web.events.addEventListener
import web.html.HTMLElement
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit
import web.storage.localStorage

fun initTabs(container: HTMLElement) {
    if (!container.isModified(TabsStyle.Initial)) return
    container.unmodify(TabsStyle.Initial)

    val viewport = container.querySelector(TabsStyle.Viewport) ?: error("no viewport found")
    val buttons = container.querySelectorAll(TabsStyle.Button)
    val panels = container.querySelectorAll(TabsStyle.Panel)
    val storageKey = container.id.toString().takeIf { it.isNotEmpty() }?.let { "${STORAGE_KEY_BASE}.$it" }
    var currentIndex = container.requireAttribute(TabsStyle.Index)

    fun changeTab(index: Int) {
        if (currentIndex == index || index !in panels.indices) return

        val startHeight = viewport.offsetHeight
        val endHeight = (panels[index] as HTMLElement).offsetHeight

        viewport.style.setProperty("height", "${startHeight}px")
        viewport.offsetHeight // a load bearing property reference

        panels[currentIndex].classList.remove(TabsStyle.IsActive.className)
        buttons[currentIndex].classList.remove(TabsStyle.IsActive.className)
        currentIndex = index

        panels[index].classList.add(TabsStyle.IsActive.className)
        buttons[index].classList.add(TabsStyle.IsActive.className)

        viewport.style.setProperty("height", "${endHeight}px")
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
            changeTab(index)
        }
    }
    observer.observe(container, MutationObserverInit(
        attributes = true,
        attributeFilter = arrayOf(TabsStyle.Index.name)
    ))

    storageKey?.let {
        localStorage.getItem(it)?.toIntOrNull()?.let { stored ->
            container.setAttribute(TabsStyle.Index.to(stored))
        }
    }
}

fun findAndInitTabs(parent: HTMLElement) {
    parent.querySelectorAll(TabsStyle.Initial).forEach { initTabs(it as HTMLElement) }
}

private const val STORAGE_KEY_BASE = "streetlight.tabValues"