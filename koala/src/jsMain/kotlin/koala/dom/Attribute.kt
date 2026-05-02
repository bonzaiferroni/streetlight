package koala.dom

import koala.core.get
import koala.html.Attribute
import koala.html.AttributeValue
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

data class ElementAttribute<T>(
    val element: HTMLElement,
    val value: T
)

fun <T> Element.queryAttributeAll(
    attribute: Attribute<T>,
): List<ElementAttribute<T>> {
    return querySelectorAll(attribute.selector).asList().mapNotNull {
        val element = it as HTMLElement
        element.toElementAttribute(attribute) ?: return@mapNotNull null
    }
}

fun <T> Element.queryAttribute(attribute: Attribute<T>) =
    querySelector(attribute)?.toElementAttribute(attribute)

private fun <T> HTMLElement.toElementAttribute(
    attribute: Attribute<T>,
): ElementAttribute<T>? {
    val value = attributes[attribute] ?: return null
    val transform = attribute.toValue ?: error("transform not found: ${attribute.identifier}")
    val data = transform(value)
    return ElementAttribute(this, data)
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
    setAttribute(attribute.key, value)

fun <T> Element.setAttribute(expression: AttributeValue<T>) =
    setAttribute(expression.attribute.key, expression.value.toString())