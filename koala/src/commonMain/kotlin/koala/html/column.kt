package koala.html

import kotlinx.html.*

inline fun FlowContent.column(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    div {
        this.id = id.value
        modify(Column, *modifiers)
        content()
    }
}

inline fun FlowContent.column(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    div {
        modify(Column, *modifiers)
        content()
    }
}