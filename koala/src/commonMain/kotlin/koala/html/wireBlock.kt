package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style

fun FlowContent.wireBlock(
    id: Id,
    modifiers: ModifierSet? = null,
    isVisibleWhileLoading: Boolean = false,
) {
    div {
        setId(id)
        addModifiers(WireBlockKey.Class, modifiers)
        if (!isVisibleWhileLoading) {
            style = "display: none;"
        }
    }
}

object WireBlockKey {
    val Class = Css("wire-block")
}

// language="CSS"
val WireBlockCss get() = """
.wire-block {
}
"""