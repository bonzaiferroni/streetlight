package koala.modifier

import js.array.asList
import koala.dom.querySelector
import web.dom.Element
import web.html.HTMLElement
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit

/** Sets [attribute] to the text [value]. */
fun Element.setAttribute(attribute: Attribute<*>, value: String) =
    setAttribute(attribute.identifier, value)

/** Sets the attribute of [expression] to its value. */
fun <T> Element.setAttribute(expression: AttributeValue<T>) =
    setAttribute(expression.attribute.identifier, expression.toStringValue())

/** The value of [attribute], or `null` when it is absent. */
fun <T> Element.getAttribute(attribute: Attribute<T>): T? = attributes.getNamedItem(attribute.identifier)?.let {
    val transform = attribute.toValue ?: error("transform not found: ${attribute.name}")
    transform(it.value)
}

/** The value of [attribute]. Throws when it is absent. */
fun <T> Element.requireAttribute(attribute: Attribute<T>): T =
    getAttribute(attribute) ?: error("$attribute not found on <${tagName.lowercase()}>")

/** The value of [attribute] on this element or its nearest ancestor that has it, or `null`. */
fun <T> Element.getClosestAttribute(attribute: Attribute<T>) = closest(attribute.selector)?.getAttribute(attribute)

/** The value of [attribute] on this element or its nearest ancestor that has it. Throws when none has it. */
fun <T> Element.requireClosestAttribute(attribute: Attribute<T>) =
    closest(attribute.selector)?.getAttribute(attribute) ?: error("$attribute not found close to <${tagName.lowercase()}>")

/** Calls [block] with the value of [attribute] each time it changes. */
fun <T> Element.observeAttribute(attribute: Attribute<T>, block: (T?) -> Unit) {
    val observer = MutationObserver({ _, _ ->
        block(getAttribute(attribute))
    })
    observer.observe(this, MutationObserverInit(
        attributes = true,
        attributeFilter = arrayOf(attribute.identifier)
    ))
}