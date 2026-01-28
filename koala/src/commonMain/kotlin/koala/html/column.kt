package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.column(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    column(*modifiers) {
        this.id = id.value
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