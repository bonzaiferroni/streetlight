package koala.modifier

import js.array.asList
import koala.dom.querySelector
import web.dom.Element
import web.html.HTMLElement
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit

fun Element.setAttribute(attribute: Attribute<*>, value: String) =
    setAttribute(attribute.identifier, value)

fun <T> Element.setAttribute(expression: AttributeValue<T>) =
    setAttribute(expression.attribute.identifier, expression.toStringValue())

fun <T> Element.getAttribute(attribute: Attribute<T>): T? = attributes.getNamedItem(attribute.identifier)?.let {
    val transform = attribute.toValue ?: error("transform not found: ${attribute.name}")
    transform(it.value)
}

fun <T> Element.requireAttribute(attribute: Attribute<T>): T =
    getAttribute(attribute) ?: error("$attribute not found on <${tagName.lowercase()}>")

fun <T> Element.getClosestAttribute(attribute: Attribute<T>) = closest(attribute.selector)?.getAttribute(attribute)

fun <T> Element.requireClosestAttribute(attribute: Attribute<T>) =
    closest(attribute.selector)?.getAttribute(attribute) ?: error("$attribute not found close to <${tagName.lowercase()}>")

fun <T> Element.observeAttribute(attribute: Attribute<T>, block: (T?) -> Unit) {
    val observer = MutationObserver({ _, _ ->
        block(getAttribute(attribute))
    })
    observer.observe(this, MutationObserverInit(
        attributes = true,
        attributeFilter = arrayOf(attribute.identifier)
    ))
}