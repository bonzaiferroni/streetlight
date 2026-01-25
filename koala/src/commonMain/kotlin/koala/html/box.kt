package koala.html

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id

inline fun FlowContent.box(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit = { },
) {
    div {
        this.id = id.value
        modify(Box, *modifiers)
        content()
    }
}

inline fun FlowContent.box(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit = { },
) {
    div {
        modify(Box, *modifiers)
        content()
    }
}