package koala.modifier

import web.cssom.CSSStyleDeclaration
import web.cssom.ElementCSSInlineStyle
import web.html.HTMLElement

/** Sets [style] in this declaration. */
fun <T: Any> CSSStyleDeclaration.setStyle(style: InlineStyle<T>) =
    setProperty(style.property.identifier, style.stringValue)

/** Sets [style] in the element's inline style. */
fun <T: Any> HTMLElement.setStyle(style: InlineStyle<T>): ElementCSSInlineStyle {
    this.style.setProperty(style.property.identifier, style.stringValue)
    return this
}

/** Removes [property] from the element's inline style. */
fun <T: Any> HTMLElement.removeStyle(property: Property<T>): HTMLElement {
    this.style.removeProperty(property.identifier)
    return this
}

/** Sets [property] to [value], or removes it when [value] is `null`. */
fun <T: Any> HTMLElement.setStyle(property: Property<T>, value: T?) {
    when (value) {
        null -> removeStyle(property)
        else -> setStyle(property.of(value))
    }
}