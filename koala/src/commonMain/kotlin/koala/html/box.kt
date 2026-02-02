package koala.html

import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id

inline fun FlowContent.box(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit = { },
) {
    div {
        applyModifiers(Box, *modifiers)
        content()
    }
}

inline fun FlowContent.box(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit = { },
) {
    box(modifiers = modifiers) {
        this.id = id.value
        content()
    }
}