package koala.dom

import koala.core.get
import koala.css.CssValue
import koala.css.StyleProperty
import koala.css.Modifier
import koala.external.ScrollIntoViewOptions
import koala.html.Attribute
import koala.html.Queryable
import koala.utils.jsonConfig
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.css.CSSStyleDeclaration

fun Element.unmodify(vararg modifier: Modifier) = modifier.forEach { classList.remove(it.value) }
fun Element.modify(vararg modifier: Modifier) = modifier.forEach { classList.add(it.value) }
fun Element.unmodify(modifiers: Collection<Modifier>) = modifiers.forEach { classList.remove(it.value) }
fun Element.modify(modifiers: Collection<Modifier>) = modifiers.forEach { classList.add(it.value) }

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

inline fun <reified T> Element.wireByAttribute(attribute: Attribute, block: (HTMLElement, T) -> Unit) {
    querySelectorAll(attribute.selector).asList().forEach {
        val element = it as HTMLElement
        val json = element.attributes[attribute] ?: return@forEach
        val data = jsonConfig.decodeFromString<T>(json)
        block(element, data)
    }
}

fun CSSStyleDeclaration.setProperty(property: StyleProperty, value: CssValue) = setProperty("--${property.identifier}", value.expression)

fun Element.querySelector(queryable: Queryable) = querySelector(queryable.selector) as? HTMLElement

fun Element.setAttribute(attribute: Attribute, value: String) = setAttribute(attribute.key, value)