package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.column(
    id: Id?,
    mod: ModifierSet? = null,
    crossinline content: DIV.() -> Unit = { },
) {
    column(mod) {
        id?.let {
            this.id = id.identifier
        }
        content()
    }
}

inline fun FlowContent.column(
    mod: ModifierSet? = null,
    crossinline content: DIV.() -> Unit = { },
) {
    div {
        addModifiers(modify(FlexColumn, mod))
        content()
    }
}