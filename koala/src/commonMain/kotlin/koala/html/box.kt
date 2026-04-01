package koala.html

import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id

fun FlowContent.box(
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        addModifiers(Box, modifiers)
        block?.invoke(this)
    }
}

fun FlowContent.box(
    id: Id,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null,
) {
    box(modifiers = modifiers) {
        this.id = id.identifier
        block?.invoke(this)
    }
}