package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.mount(
    id: Id,
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null,
) {
    box(id, mod, block)
}