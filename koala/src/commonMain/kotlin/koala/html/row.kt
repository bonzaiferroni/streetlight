package koala.html

import kotlinx.html.*
import koala.css.*

object Row : CssClass { override val value = "layout-row" }

inline fun FlowContent.row(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    div {
        this.id = id.value
        modify(Row, *modifiers)
        content()
    }
}

inline fun FlowContent.row(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    div {
        modify(Row, *modifiers)
        content()
    }
}