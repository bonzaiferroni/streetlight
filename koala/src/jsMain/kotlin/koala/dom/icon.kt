package koala.dom

import koala.SvgFile
import koala.css.*
import koala.html.IconElement
import koala.html.configureIcon
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

fun DOMContext.icon(
    file: SvgFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) = div {
    configureIcon(
        file = file,
        modifiers = modifiers,
        block = block
    )
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