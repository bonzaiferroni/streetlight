package koala.core

import js.array.asList
import koala.modifier.Clickable
import koala.dom.elementId
import koala.modifier.modify
import koala.modifier.Attribute
import koala.html.Id
import koala.html.Queryable
import web.dom.Document
import web.dom.Element
import web.dom.NamedNodeMap
import web.dom.document
import web.events.addEventListener
import web.html.HTMLElement
import web.pointer.CLICK
import web.pointer.PointerEvent

fun Document.queryAll(modifier: Queryable) = querySelectorAll(modifier.selector).asList()

/** Calls [onClick] on each click of the element with [id], and marks it clickable. */
fun Document.onClick(id: Id, onClick: () -> Unit) = querySelector(id.identifier)?.let {
    val element = it as HTMLElement
    element.modify(Clickable)
    element.addEventListener(PointerEvent.CLICK, {
        onClick()
    })
}

/** The value of [attribute] on the first element that has it, or `null`. */
fun Document.queryAttribute(attribute: Attribute<*>) = querySelector(attribute.selector)?.attributes?.get(attribute)

fun Element.queryAll(modifier: Queryable) = querySelectorAll(modifier.selector).asList()

fun Element.queryFirstOrNull(modifier: Queryable) = querySelector(modifier.selector) as? HTMLElement

/** Appends a new `div` with [id] and returns it. */
fun Element.appendDiv(id: Id? = null): HTMLElement {
    val element = document.createElement("div")
    if (id != null) {
        element.id = id.elementId
    }
    append(element)
    return element
}

operator fun NamedNodeMap.get(attribute: Attribute<*>): String? = this.getNamedItem(attribute.identifier)?.value
