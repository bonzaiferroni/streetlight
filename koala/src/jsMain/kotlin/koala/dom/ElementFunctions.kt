package koala.dom

import koala.css.DisplayNone
import koala.css.InlineStyle
import koala.css.Property
import koala.css.Modifier
import koala.html.Queryable
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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

fun Element.toggle(modifier: Modifier) = classList.toggle(modifier.identifier)

fun Element.querySelector(queryable: Queryable) = querySelector(queryable.selector) as? HTMLElement
fun Element.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList().map {
    it as HTMLElement
}

fun querySelector(queryable: Queryable) = document.body!!.querySelector(queryable)
fun querySelectorAll(queryable: Queryable) = document.body!!.querySelectorAll(queryable)

fun CSSStyleDeclaration.removeProperty(property: Property<*>) = removeProperty(property.identifier)

