package koala.dom

import koala.html.Id
import org.w3c.dom.Document
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import kotlin.js.Promise

fun Document.getElementById(id: Id) = getElementById(id.identifier) as HTMLElement
fun Document.getElementOrNullById(id: Id) = getElementById(id.identifier) as? HTMLElement

fun Document.startViewTransition(updateCallback: () -> Unit): ViewTransition =
    this.asDynamic().startViewTransition(updateCallback).unsafeCast<ViewTransition>()

fun Document.closeOpenPopovers() = querySelectorAll("[popover]:popover-open").asList().forEach { node ->
    (node as? HTMLElement)?.closePopover()
}

external interface ViewTransition {
    val ready: Promise<Unit>
    val finished: Promise<Unit>
    val updateCallbackDone: Promise<Unit>
    fun skipTransition()
}

