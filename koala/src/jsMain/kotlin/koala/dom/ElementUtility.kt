package koala.dom

import kampfire.utils.takeEllipsis
import koala.css.Property
import koala.css.Modifier
import koala.html.Queryable
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.css.CSSStyleDeclaration

fun <T: Element> T.unmodify(vararg modifier: Modifier): T {
    modifier.forEach { classList.remove(it.identifier) }
    return this
}
fun <T: Element> T.modify(vararg modifier: Modifier): T {
    modifier.forEach { classList.add(it.identifier) }
    return this
}
fun <T: Element> T.unmodify(modifiers: Collection<Modifier>): T {
    modifiers.forEach { classList.remove(it.identifier) }
    return this
}
fun <T: Element> T.modify(modifiers: Collection<Modifier>): T {
    modifiers.forEach { classList.add(it.identifier) }
    return this
}

fun <T: Element> T.trigger(modifier: Modifier): T {
    unmodify(modifier)
    window.requestAnimationFrame {
        modify(modifier)
    }
    return this
}

fun <T: Element> T.modifyAfterFrame(vararg modifier: Modifier): T {
    window.requestAnimationFrame {
        modify(*modifier)
    }
    return this
}

fun <T: Element> T.unmodifyAfterFrame(vararg modifier: Modifier): T {
    window.requestAnimationFrame {
        unmodify(*modifier)
    }
    return this
}

fun Element.isModified(modifier: Modifier) = classList.contains(modifier.identifier)
fun Element.getAncestor(modifier: Modifier) = closest(modifier.selector) as? HTMLElement

fun Element.toggle(modifier: Modifier) = classList.toggle(modifier.identifier)

fun Element.querySelector(queryable: Queryable) = querySelector(queryable.selector) as? HTMLElement
fun Element.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList().map {
    it as HTMLElement
}

fun querySelector(queryable: Queryable) = document.body!!.querySelector(queryable)
fun querySelectorAll(queryable: Queryable) = document.body!!.querySelectorAll(queryable)

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
        val name = current.id.takeIf { it.isNotEmpty() }?.let { "#$it" } ?: current.tagName.lowercase()
        insert(0, name)
        current = current.parentElement
    }
}