package koala.dom

import koala.Svg
import koala.css.*
import koala.html.configureIcon
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

fun TagScope.icon(
    file: Svg,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) = div {
    configureIcon(
        file = file,
        modifiers = mod,
        block = block
    )
}

fun TagScope.icon(
    file: Svg,
    onClick: () -> Unit,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
): HTMLDivElement {
    val element = icon(file, mod, block)
    element.onClick {
        onClick()
    }
    return element
}