package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.html.Attribute
import koala.html.blockLabel
import koala.html.setAttribute
import kotlinx.html.DIV
import kotlinx.html.js.div

@Deprecated("use attribute setter")
fun DOMContext.blockLabel(
    label: String,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) = div {
    this.blockLabel = label
    addModifiers(modifiers)
    setAttribute(Attribute.BlockLabel, label)
    block?.invoke(this)
}