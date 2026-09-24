package koala.html

import kotlinx.html.*
import koala.modifier.*

/** A flex [row] with [id]. */
fun FlowContent.row(
    id: Id,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {},
) {
    row(mod) {
        this.id = id.identifier
        block()
    }
}

/** A flex row. */
fun FlowContent.row(
    mod: Modifier? = null,
    block: DIV.() -> Unit = {},
) {
    div {
        addModifiers(FlexRow, mod)
        block()
    }
}