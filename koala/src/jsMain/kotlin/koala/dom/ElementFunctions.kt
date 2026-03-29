package koala.dom

import koala.core.get
import koala.css.InlineStyle
import koala.css.Property
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

fun Element.unmodify(vararg modifier: Modifier) = modifier.forEach { classList.remove(it.identifier) }
fun Element.modify(vararg modifier: Modifier) = modifier.forEach { classList.add(it.identifier) }
fun Element.unmodify(modifiers: Collection<Modifier>) = modifiers.forEach { classList.remove(it.identifier) }
fun Element.modify(modifiers: Collection<Modifier>) = modifiers.forEach { classList.add(it.identifier) }

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

fun <T> Element.queryAttributeAll(
    attribute: Attribute<T>,
    provider: (String) -> T,
): List<Pair<HTMLElement, T>> {
    return querySelectorAll(attribute.selector).asList().mapNotNull {
        val element = it as HTMLElement
        val value = element.attributes[attribute] ?: return@mapNotNull null
        val data = provider(value)
        element to data
    }
}

inline fun <reified T> Element.queryJsonAttribute(attribute: Attribute<T>): List<Pair<HTMLElement, T>> {
    return queryAttributeAll(attribute, { json ->
        jsonConfig.decodeFromString<T>(json)
    })
}

fun <T> CSSStyleDeclaration.setProperty(style: InlineStyle<T>) =
    setProperty(style.property.expression, style.value.toString())

fun Element.querySelector(queryable: Queryable) = querySelector(queryable.selector) as? HTMLElement
fun Element.querySelectorAll(queryable: Queryable) = querySelectorAll(queryable.selector).asList().map {
    it as HTMLElement
}

fun Element.setAttribute(attribute: Attribute<*>, value: String) = setAttribute(attribute.key, value)

fun CSSStyleDeclaration.removeProperty(property: Property<*>) = removeProperty(property.identifier)