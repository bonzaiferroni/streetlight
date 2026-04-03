package koala.dom

import koala.core.get
import koala.html.Attribute
import koala.html.AttributeExpression
import koala.utils.jsonConfig
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

data class ElementAttributeValue<T>(
    val element: HTMLElement,
    val value: T
)

fun <T> Element.queryAttributeAll(
    attribute: Attribute<T>,
    transform: (String) -> T,
): List<ElementAttributeValue<T>> {
    return querySelectorAll(attribute.selector).asList().mapNotNull {
        val element = it as HTMLElement
        element.toElementAttribute(attribute, transform) ?: return@mapNotNull null
    }
}

fun <T> Element.queryAttribute(attribute: Attribute<T>, transform: (String) -> T) =
    querySelector(attribute)?.toElementAttribute(attribute, transform)

private fun <T> HTMLElement.toElementAttribute(
    attribute: Attribute<T>,
    transform: (String) -> T,
): ElementAttributeValue<T>? {
    val value = attributes[attribute] ?: return null
    val data = transform(value)
    return ElementAttributeValue(this, data)
}

inline fun <reified T> Element.queryJsonAttributeAll(attribute: Attribute<T>): List<ElementAttributeValue<T>> {
    return queryAttributeAll(attribute) { json ->
        jsonConfig.decodeFromString<T>(json)
    }
}

inline fun <reified T> Element.queryJsonAttribute(attribute: Attribute<T>): ElementAttributeValue<T>? {
    return queryAttribute(attribute) { json ->
        jsonConfig.decodeFromString<T>(json)
    }
}

fun Element.setAttribute(attribute: Attribute<*>, value: String) =
    setAttribute(attribute.key, value)

fun <T> Element.setAttribute(expression: AttributeExpression<T>) =
    setAttribute(expression.attribute.key, expression.value.toString())