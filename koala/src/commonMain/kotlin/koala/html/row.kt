package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.row(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    row(*modifiers) {
        this.id = id.value
        content()
    }
}

inline fun FlowContent.row(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    div {
        applyModifiers(Row, *modifiers)
        content()
    }
}