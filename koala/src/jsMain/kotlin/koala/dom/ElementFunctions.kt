package koala.dom

import koala.core.get
import koala.css.InlineStyle
import koala.css.Property
import koala.css.Modifier
import koala.external.ScrollIntoViewOptions
import koala.html.Attribute
import koala.html.AttributeExpression
import koala.html.Queryable
import koala.utils.jsonConfig
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.css.CSSStyleDeclaration

fun Element.unmodify(vararg modifier: Modifier) = modifier.forEach { classList.remove(it.identifier) }
fun Element.modify(vararg modifier: Modifier) = modifier.forEach { classList.add(it.identifier) }
fun Element.unmodify(modifiers: Collection<Modifier>) = modifiers.forEach { classList.remove(it.identifier) }
fun Element.modify(modifiers: Collection<Modifier>) = modifiers.forEach { classList.add(it.identifier) }

fun Element.isModified(modifier: Modifier) = classList.contains(modifier.identifier)

fun Node.modify(vararg modifier: Modifier) {
    val element = this as? Element ?: error("not an element")
    element.modify(*modifier)
}

fun Node.unmodify(vararg modifier: Modifier) {
    val element = this as? Element ?: error("not an element")
    element.unmodify(*modifier)
}


private const val MAX_ATTEMPTS = 30

fun Element.scrollWhenPresent(
    options: ScrollIntoViewOptions? = ScrollIntoViewOptions(
        behavior = "smooth",
        block = "nearest"
    )
) {
    var attempts = 0

    fun tryScroll() {
        if (isConnected) {
            scrollIntoView(options)
            return
        }

        if (++attempts >= MAX_ATTEMPTS) return

        window.requestAnimationFrame { tryScroll() }
    }

    window.requestAnimationFrame { tryScroll() }
}

fun <T> CSSStyleDeclaration.setProperty(style: InlineStyle<T>) =
    setProperty(style.property.expression, style.value.toString())

fun Element.querySelector(queryable: Queryable) = querySelector(queryable.selector) as? HTMLElement
fun Element.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList().map {
    it as HTMLElement
}

fun CSSStyleDeclaration.removeProperty(property: Property<*>) = removeProperty(property.identifier)