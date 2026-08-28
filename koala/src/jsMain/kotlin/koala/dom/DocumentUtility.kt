package koala.dom

import js.array.asList
import js.promise.Promise
import koala.html.Id
import web.dom.Document
import web.dom.ElementId
import web.viewtransition.ViewTransition

val Id.elementId get() = ElementId(identifier)

fun Document.getElementById(id: Id) = getElementById(id.elementId) ?: error("Element not found: $id")
fun Document.getElementOrNullById(id: String) = getElementById(ElementId(id))
fun Document.getElementOrNullById(id: Id) = getElementById(id.elementId)

fun Document.viewTransition(updateCallback: () -> Unit): ViewTransition =
    startViewTransition {
        updateCallback()
        null
    }

fun Document.closeOpenPopovers() = querySelectorAll("[popover]:popover-open").asList().forEach { node ->
    node.closePopover()
}

