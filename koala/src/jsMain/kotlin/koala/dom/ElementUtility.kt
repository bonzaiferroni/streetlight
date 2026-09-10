package koala.dom

import js.array.asList
import kampfire.utils.takeEllipsis
import koala.css.Property
import koala.css.Modifier
import koala.html.Queryable
import web.animations.requestAnimationFrame
import web.cssom.CSSStyleDeclaration
import web.cssom.ClassName
import web.dom.Document
import web.dom.Element
import web.dom.document
import web.html.HTMLElement

fun <T: Element> T.unmodify(vararg modifier: Modifier): T {
    modifier.forEach { classList.remove(it.className) }
    return this
}
fun <T: Element> T.modify(vararg modifier: Modifier): T {
    modifier.forEach { classList.add(it.className) }
    return this
}
fun <T: Element> T.unmodify(modifiers: Collection<Modifier>): T {
    modifiers.forEach { classList.remove(it.className) }
    return this
}
fun <T: Element> T.modify(modifiers: Collection<Modifier>): T {
    modifiers.forEach { classList.add(it.className) }
    return this
}

fun <T: Element> T.setModifiers(vararg modifier: Modifier): T {
    className = ClassName(modifier.joinToString(" ") { it.identifier })
    return this
}

val Modifier.className get() = ClassName(identifier)

fun <T: Element> T.trigger(modifier: Modifier): T {
    unmodify(modifier)
    requestAnimationFrame {
        modify(modifier)
    }
    return this
}

fun <T: Element> T.modifyAfterFrame(vararg modifier: Modifier): T {
    requestAnimationFrame {
        modify(*modifier)
    }
    return this
}

fun <T: Element> T.unmodifyAfterFrame(vararg modifier: Modifier): T {
    requestAnimationFrame {
        unmodify(*modifier)
    }
    return this
}

fun Element.isModified(modifier: Modifier) = classList.contains(modifier.className)
fun Element.toggle(modifier: Modifier) = classList.toggle(modifier.className)

// td: remove cast
fun Element.querySelector(queryable: Queryable) = querySelector(queryable.selector) as? HTMLElement
fun Element.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList()

fun Document.querySelector(queryable: Queryable) = querySelector(queryable.selector)
fun Document.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList()

fun Element.closest(queryable: Queryable) = closest(queryable.selector)
fun Element.requireClosest(queryable: Queryable) = closest(queryable.selector) as? HTMLElement
    ?: error("closest not found: ${queryable.selector}")

fun Element.asHtmlElement() = this as HTMLElement

fun querySelector(queryable: Queryable) = document.body.querySelector(queryable)
fun querySelectorAll(queryable: Queryable) = document.body.querySelectorAll(queryable)

fun CSSStyleDeclaration.removeStyle(property: Property<*>) = removeProperty(property.identifier)

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

