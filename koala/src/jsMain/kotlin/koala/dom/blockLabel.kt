package koala.dom

import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.html.blockLabel
import kotlinx.html.DIV
import kotlinx.html.js.div

fun DOMContext.blockLabel(
    label: String,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit?)? = null
) = div {
    this.blockLabel = label
    applyModifiers(modifiers)
    block?.invoke(this)
}