package koala.dom

import koala.SvgFile
import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

fun DOMContext.icon(
    file: SvgFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) = div {
    applyModifiers(modify(ElementClass.icon, modifiers))
    applyStyles(styleOf(StyleProperty.maskSrc to UrlValue(file)))
    block?.invoke(this)
}

fun DOMContext.icon(
    file: SvgFile,
    onClick: () -> Unit,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
): HTMLDivElement {
    val element = icon(file, modifiers, block)
    element.onClick(onClick)
    return element
}