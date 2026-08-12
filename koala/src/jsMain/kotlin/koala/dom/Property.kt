package koala.dom

import koala.css.InlineStyle
import koala.css.Property
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.css.ElementCSSInlineStyle
import org.w3c.dom.svg.SVGSVGElement

fun <T: Any> CSSStyleDeclaration.setStyle(style: InlineStyle<T>) =
    setProperty(style.property.expression, style.valueString)

fun <T: Any> HTMLElement.setStyle(style: InlineStyle<T>): ElementCSSInlineStyle {
    this.style.setProperty(style.property.expression, style.valueString)
    return this
}

fun <T: Any> HTMLElement.removeStyle(property: Property<T>): HTMLElement {
    this.style.removeProperty(property.identifier)
    return this
}