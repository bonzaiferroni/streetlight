package koala.html

import koala.modifier.*
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style

/** An element with [id] that the browser wires a view into, hidden until then unless [isVisibleWhileLoading]. */
fun FlowContent.wiringBlock(
    id: Id,
    mod: Modifier? = null,
    isVisibleWhileLoading: Boolean = false,
) {
    div {
        setId(id)
        addModifiers(WireBlockKey.Class, mod)
        if (!isVisibleWhileLoading) {
            style = "display: none;"
        }
    }
}

object WireBlockKey {
    val Class = Class("wire-block")
}

// language="CSS"
val WireBlockCss get() = """
.wire-block {
}
"""