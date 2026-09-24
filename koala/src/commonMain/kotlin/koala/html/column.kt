package koala.html

import kotlinx.html.*
import koala.modifier.*

/** A flex [column] with [id]. */
fun FlowContent.column(
    id: Id?,
    mod: Modifier? = null,
    content: DIV.() -> Unit = { },
) {
    column(mod) {
        id?.let {
            this.id = id.identifier
        }
        content()
    }
}

/** A flex column. */
fun FlowContent.column(
    mod: Modifier? = null,
    content: DIV.() -> Unit = { },
) {
    div {
        addModifiers(modify(FlexColumn, mod))
        content()
    }
}