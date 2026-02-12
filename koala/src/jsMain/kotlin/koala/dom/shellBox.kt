package koala.dom

import koala.css.*
import kotlinx.html.DIV
import org.w3c.dom.HTMLDivElement

fun RenderContext.shellBox(
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit
): HTMLDivElement {
    val element = box {
        applyModifiers(modifiers)
        block()
    }

    findAndInitTabs(element)

    return element
}