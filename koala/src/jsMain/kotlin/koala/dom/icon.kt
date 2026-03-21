package koala.dom

import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.style
import org.w3c.dom.HTMLDivElement

fun DOMContext.icon(
    src: String,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) = div {
    applyModifiers(modify(ElementClass.icon, modifiers))
    style = styleOf(CustomProperty.maskSrc, UrlValue(src))
    block?.invoke(this)
}

fun DOMContext.icon(
    src: String,
    onClick: () -> Unit,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
): HTMLDivElement {
    val element = icon(src, modifiers, block)
    element.onClick(onClick)
    return element
}