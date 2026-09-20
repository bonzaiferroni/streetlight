package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.box(
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        addModifiers(Box, mod)
        block?.invoke(this)
    }
}

fun FlowContent.box(
    id: Id?,
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null,
) {
    box(mod = mod) {
        setId(id)
        block?.invoke(this)
    }
}