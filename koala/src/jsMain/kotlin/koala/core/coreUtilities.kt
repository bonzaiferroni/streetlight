package koala.core

import js.array.asList
import koala.css.Clickable
import koala.css.Modifier
import koala.dom.elementId
import koala.dom.modify
import koala.html.Attribute
import koala.html.Id
import web.dom.Document
import web.dom.Element
import web.dom.NamedNodeMap
import web.dom.document
import web.events.addEventListener
import web.html.HTMLElement
import web.pointer.CLICK
import web.pointer.PointerEvent

fun Document.queryAll(modifier: Modifier) = querySelectorAll(modifier.selector).asList()

fun Document.onClickElementAll(modifier: Modifier, onClick: (HTMLElement) -> Unit) = querySelectorAll(modifier.selector).asList()
    .forEach {
        val element = it as HTMLElement
        element.modify(Clickable)
        element.addEventListener(PointerEvent.CLICK, {
            onClick(element)
        })
    }

fun Document.onClick(id: Id, onClick: () -> Unit) = querySelector(id.identifier)?.let {
    val element = it as HTMLElement
    element.modify(Clickable)
    element.addEventListener(PointerEvent.CLICK, {
        onClick()
    })
}

fun Document.queryAttribute(attribute: Attribute<*>) = querySelector(attribute.selector)?.attributes?.get(attribute)

fun Element.queryAll(modifier: Modifier) = querySelectorAll(modifier.selector).asList()

fun Element.queryFirstOrNull(modifier: Modifier) = querySelector(modifier.selector) as? HTMLElement

fun Element.appendDiv(id: Id? = null): HTMLElement {
    val element = document.createElement("div")
    if (id != null) {
        element.id = id.elementId
    }
    append(element)
    return element
}

operator fun NamedNodeMap.get(attribute: Attribute<*>): String? = this.getNamedItem(attribute.identifier)?.value
