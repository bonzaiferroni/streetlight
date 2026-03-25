package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.row(
    id: Id,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {},
) {
    row(modifiers) {
        this.id = id.value
        block()
    }
}

fun FlowContent.row(
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {},
) {
    div {
        setModifiers(Row, modifiers)
        block()
    }
}