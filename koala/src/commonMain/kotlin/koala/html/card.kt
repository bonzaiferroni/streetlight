package koala.html

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id

inline fun FlowContent.card(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    div {
        this.id = id.value
        modify(Card, *modifiers)
        content()
    }
}

inline fun FlowContent.card(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    div {
        modify(Card, *modifiers)
        content()
    }
}