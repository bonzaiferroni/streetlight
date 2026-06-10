package koala.dom

import koala.css.InlineStyle
import koala.css.Property
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration

fun <T: Any> CSSStyleDeclaration.setProperty(style: InlineStyle<T>) =
    setProperty(style.property.expression, style.valueString)

fun <T: Any> HTMLElement.setProperty(style: InlineStyle<T>): HTMLElement {
    this.style.setProperty(style.property.expression, style.valueString)
    return this
}

fun <T: Any> HTMLElement.removeProperty(property: Property<T>): HTMLElement {
    this.style.removeProperty(property.identifier)
    return this
}