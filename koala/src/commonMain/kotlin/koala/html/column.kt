package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.column(
    id: Id?,
    mod: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) {
    column(mod) {
        id?.let {
            this.id = id.identifier
        }
        content()
    }
}

fun FlowContent.column(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) {
    div {
        addModifiers(modify(FlexColumn, mod))
        content()
    }
}