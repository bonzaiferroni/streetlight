package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.row(
    id: Id,
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) {
    row(modifiers) {
        this.id = id.value
        content()
    }
}

inline fun FlowContent.row(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) {
    div {
        applyModifiers(Row, modifiers)
        content()
    }
}