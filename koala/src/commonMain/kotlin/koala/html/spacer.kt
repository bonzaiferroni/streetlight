package koala.html

import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.spacer(
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        applyModifiers(modifiers)
        block?.invoke(this)
    }
}