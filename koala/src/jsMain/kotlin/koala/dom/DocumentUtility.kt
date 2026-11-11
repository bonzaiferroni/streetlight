package koala.dom

import js.array.asList
import js.promise.Promise
import koala.page.Id
import web.dom.Document
import web.dom.ElementId
import web.viewtransition.ViewTransition
import koala.dom.component.closePopover
import koala.dom.component.open
import koala.dom.component.popover

val Id.elementId get() = ElementId(identifier)

/** The element with [id]. Throws when there is none. */
fun Document.getElementById(id: Id) = getElementById(id.elementId) ?: error("Element not found: $id")
fun Document.getElementOrNullById(id: String) = getElementById(ElementId(id))
fun Document.getElementOrNullById(id: Id) = getElementById(id.elementId)

/** The view transition waiting to capture the page and the updates it will run, or `null` when none waits. */
private var pending: PendingTransition? = null

private class PendingTransition(val updates: MutableList<() -> Unit>) {
    lateinit var transition: ViewTransition
}

/**
 * Runs [updateCallback] inside a view transition, returning it.
 *
 * A call made while a transition waits to capture the page joins it and returns it, so changes from one state update
 * animate together. The updates run in the order they were called.
 */
fun Document.viewTransition(updateCallback: () -> Unit): ViewTransition {
    pending?.let {
        it.updates += updateCallback
        return it.transition
    }
    val next = PendingTransition(mutableListOf(updateCallback))
    pending = next
    next.transition = startViewTransition {
        pending = null
        // each update runs even when one before it fails, and the first failure is thrown after
        val failures = next.updates.mapNotNull { update -> runCatching(update).exceptionOrNull() }
        failures.firstOrNull()?.let { throw it }
        null
    }
    return next.transition
}

/** Closes every open popover. */
fun Document.closeOpenPopovers() = querySelectorAll("[popover]:popover-open").asList().forEach { node ->
    node.closePopover()
}

