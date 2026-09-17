package koala.modifier

import web.cssom.CSSStyleDeclaration
import web.cssom.ElementCSSInlineStyle
import web.html.HTMLElement

fun <T: Any> CSSStyleDeclaration.setStyle(style: InlineStyle<T>) =
    setProperty(style.property.identifier, style.stringValue)

fun <T: Any> HTMLElement.setStyle(style: InlineStyle<T>): ElementCSSInlineStyle {
    this.style.setProperty(style.property.identifier, style.stringValue)
    return this
}

fun <T: Any> HTMLElement.removeStyle(property: Property<T>): HTMLElement {
    this.style.removeProperty(property.identifier)
    return this
}

fun <T: Any> HTMLElement.setStyle(property: Property<T>, value: T?) {
    when (value) {
        null -> removeStyle(property)
        else -> setStyle(property.of(value))
    }
}