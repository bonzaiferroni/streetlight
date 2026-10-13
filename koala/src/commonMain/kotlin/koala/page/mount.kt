package koala.page

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import koala.component.box

/** An empty element with [id], for the browser to mount a view into. */
fun FlowContent.mount(
    id: Id,
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null,
) {
    box(id, mod, block)
}