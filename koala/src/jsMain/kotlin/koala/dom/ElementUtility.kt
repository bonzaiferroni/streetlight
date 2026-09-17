package koala.dom

import js.array.asList
import kampfire.utils.takeEllipsis
import koala.modifier.Property
import koala.html.Queryable
import web.cssom.CSSStyleDeclaration
import web.dom.Document
import web.dom.Element
import web.dom.document
import web.html.HTMLElement



// td: remove cast
fun Element.querySelector(queryable: Queryable) = querySelector(queryable.selector) as? HTMLElement
fun Element.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList()

fun Document.querySelector(queryable: Queryable) = querySelector(queryable.selector)
fun Document.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList()

fun Element.closest(queryable: Queryable) = closest(queryable.selector)
fun Element.requireClosest(queryable: Queryable) = closest(queryable.selector) as? HTMLElement
    ?: error("closest not found: ${queryable.selector}")

fun Element.asHtmlElement() = this as HTMLElement

@Deprecated("query from document or element")
fun querySelector(queryable: Queryable) = document.body.querySelector(queryable)

fun CSSStyleDeclaration.removeStyle(property: Property<*>) = removeProperty(property.name)

fun Element.getPath(subject: Any? = null, limit: Int = Int.MAX_VALUE): String = buildString {
    subject?.let {
        append("[${it.toString().takeEllipsis(40)}]")
    }
    var count = 0
    var current: Element? = this@getPath
    while (current != null) {
        if (++count > limit) break
        if (isNotEmpty()) insert(0, " > ")
        val name = current.id.takeIf { it.toString().isNotEmpty() }?.let { "#$it" } ?: current.tagName.lowercase()
        insert(0, name)
        current = current.parentElement
    }
}

