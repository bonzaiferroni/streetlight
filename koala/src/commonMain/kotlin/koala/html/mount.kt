package koala.html

import koala.css.ModifierSet
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.mount(
    id: Id,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null,
) {
    box(id, modifiers, block)
}