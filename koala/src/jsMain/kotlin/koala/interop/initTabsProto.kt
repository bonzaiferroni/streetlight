package koala.interop

import koala.css.Property
import koala.dom.isModified
import koala.dom.modify
import koala.dom.querySelector
import koala.dom.querySelectorAll
import koala.dom.requireAttribute
import koala.dom.setAttribute
import koala.dom.setStyle
import koala.dom.unmodify
import koala.html.TabsProtoStyle
import kotlinx.css.LinearDimension
import kotlinx.css.px
import web.cssom.TRANSITION_END
import web.cssom.TransitionEvent
import web.events.CLICK
import web.events.Event
import web.events.addEventListener
import web.html.HTMLElement
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit
import web.storage.localStorage

fun initTabsProto(container: HTMLElement) {
    if (!container.isModified(TabsProtoStyle.Initial)) return
    container.unmodify(TabsProtoStyle.Initial)

    val viewport = container.querySelector(TabsProtoStyle.Viewport) ?: error("no viewport found")
    val buttons = container.querySelectorAll(TabsProtoStyle.Button)
    val panels = container.querySelectorAll(TabsProtoStyle.Panel)
    val storageKey = container.id.toString().takeIf { it.isNotEmpty() }?.let { "${STORAGE_KEY_BASE}.$it" }
    var currentIndex = container.requireAttribute(TabsProtoStyle.Index)

    fun changeTab(index: Int) {
        if (currentIndex == index || index !in panels.indices) return

        val startHeight = viewport.offsetHeight
        val endHeight = (panels[index] as HTMLElement).offsetHeight

        viewport.setStyle(Property.Height.to(startHeight.px))
        viewport.offsetHeight

        panels[currentIndex].unmodify(TabsProtoStyle.IsActive)
        buttons[currentIndex].unmodify(TabsProtoStyle.IsActive)
        currentIndex = index

        container.setAttribute(TabsProtoStyle.Index.to(index))
        panels[index].modify(TabsProtoStyle.IsActive)
        buttons[index].modify(TabsProtoStyle.IsActive)

        viewport.setStyle(Property.Height.to(endHeight.px))

        storageKey?.let {
            localStorage.setItem(it, index.toString())
        }
    }

    storageKey?.let {
        localStorage.getItem(storageKey)?.toIntOrNull()?.let {
            changeTab(it)
        }
    }

    buttons.forEachIndexed { index, button ->
        button.addEventListener(Event.CLICK, {
            changeTab(index)
        })
    }

    viewport.addEventListener(TransitionEvent.TRANSITION_END, { event ->
        if (event.target == viewport && event.propertyName == "height") {
            viewport.setStyle(Property.Height.to(LinearDimension.auto))
        }
    })

    val observer = MutationObserver { _, _ ->
        changeTab(container.requireAttribute(TabsProtoStyle.Index))
    }
    observer.observe(container, MutationObserverInit(
        attributes = true,
        attributeFilter = arrayOf(TabsProtoStyle.Index.name)
    ))
}

fun findAndInitTabsProto(parent: HTMLElement) {
    parent.querySelectorAll(TabsProtoStyle.Initial).forEach { initTabsProto(it as HTMLElement) }
}

private const val STORAGE_KEY_BASE = "streetlight.tabValues"