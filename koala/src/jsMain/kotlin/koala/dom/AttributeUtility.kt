package koala.dom

import js.array.asList
import koala.html.Attribute
import koala.html.AttributeValue
import web.dom.Element
import web.html.HTMLElement
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit

data class ElementAttribute<T>(
    val element: HTMLElement,
    val value: T
)

fun <T> Element.queryAttributeAll(
    attribute: Attribute<T>,
): List<ElementAttribute<T>> {
    return querySelectorAll(attribute.selector).asList().mapNotNull {
        val element = it as HTMLElement
        element.getElementAttribute(attribute) ?: return@mapNotNull null
    }
}

fun <T> Element.queryAttribute(attribute: Attribute<T>) =
    querySelector(attribute)?.getElementAttribute(attribute)

private fun <T> HTMLElement.getElementAttribute(
    attribute: Attribute<T>,
): ElementAttribute<T>? {
    val value = getAttribute(attribute) ?: return null
    return ElementAttribute(this, value)
}

// dep
//inline fun <reified T> Element.queryJsonAttributeAll(attribute: Attribute<T>): List<ElementAttributeValue<T>> {
//    return queryAttributeAll(attribute) { json ->
//        jsonConfig.decodeFromString<T>(json)
//    }
//}
//
//inline fun <reified T> Element.queryJsonAttribute(attribute: Attribute<T>): ElementAttributeValue<T>? {
//    return queryAttribute(attribute) { json ->
//        jsonConfig.decodeFromString<T>(json)
//    }
//}

fun Element.setAttribute(attribute: Attribute<*>, value: String) =
    setAttribute(attribute.identifier, value)

fun <T> Element.setAttribute(expression: AttributeValue<T>) =
    setAttribute(expression.attribute.identifier, expression.value.toString())

fun Element.toggleAttribute(attribute: Attribute<Boolean>): Boolean {
    val value = getAttribute(attribute) ?: true
    setAttribute(attribute.to(value))
    return value
}

fun <T> Element.getAttribute(attribute: Attribute<T>): T? = attributes.getNamedItem(attribute.identifier)?.let {
    val transform = attribute.toValue ?: error("transform not found: ${attribute.name}")
    transform(it.value)
}

fun <T> Element.requireAttribute(attribute: Attribute<T>): T =
    getAttribute(attribute) ?: error("$attribute not found on <${tagName.lowercase()}>")

fun <T> Element.getClosestAttribute(attribute: Attribute<T>) = closest(attribute.selector)?.getAttribute(attribute)

fun <T> Element.requireClosestAttribute(attribute: Attribute<T>) =
    closest(attribute.selector)?.getAttribute(attribute) ?: error("$attribute not found on <${tagName.lowercase()}>")

fun <T> Element.observeAttribute(attribute: Attribute<T>, block: (T?) -> Unit) {
    val observer = MutationObserver({ _, _ ->
        block(getAttribute(attribute))
    })
    observer.observe(this, MutationObserverInit(
        attributes = true,
        attributeFilter = arrayOf(attribute.identifier)
    ))
}