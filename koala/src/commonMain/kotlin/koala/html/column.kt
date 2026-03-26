package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.column(
    id: Id,
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) {
    column(modifiers) {
        this.id = id.value
        content()
    }
}

inline fun FlowContent.column(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) {
    div {
        addModifiers(modify(Column, modifiers))
        content()
    }
}