package koala.dom

import koala.css.InlineStyle
import koala.css.Property
import web.cssom.CSSStyleDeclaration
import web.cssom.ElementCSSInlineStyle
import web.html.HTMLElement

fun <T: Any> CSSStyleDeclaration.setStyle(style: InlineStyle<T>) =
    setProperty(style.property.expression, style.valueString)

fun <T: Any> HTMLElement.setStyle(style: InlineStyle<T>): ElementCSSInlineStyle {
    this.style.setProperty(style.property.expression, style.valueString)
    return this
}

fun <T: Any> HTMLElement.removeStyle(property: Property<T>): HTMLElement {
    this.style.removeProperty(property.expression)
    return this
}

fun <T: Any> HTMLElement.setStyle(property: Property<T>, value: T?) {
    when (value) {
        null -> removeStyle(property)
        else -> setStyle(property.to(value))
    }
}