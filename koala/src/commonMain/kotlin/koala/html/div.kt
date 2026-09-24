package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div as divTag

/** A `div` with [mod] applied. */
fun FlowContent.div(
    mod: Modifier? = null,
    block: DIV.() -> Unit = { },
) {
    divTag {
        addModifiers(mod)
        block()
    }
}

/** A `div` with [id] and [mod] applied. */
fun FlowContent.div(
    id: Id,
    mod: Modifier? = null,
    block: DIV.() -> Unit = { },
) {
    divTag {
        setId(id)
        addModifiers(mod)
        block()
    }
}