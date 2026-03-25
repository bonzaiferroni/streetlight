package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.setModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.swapBlock(
    id: Id,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    div {
        setId(id)
        setModifiers(modifiers, SwapBlockKey.Class)
        block()
    }
}

object SwapBlockKey {
    val Class = Css("swap-block")
}

// language="CSS"
val SwapBlockStyle get() = """
.swap-block {
    display: grid;
    min-width: 0;
}

.swap-block > * {
    grid-area: 1 / 1 / 2 / 2;
}
"""
