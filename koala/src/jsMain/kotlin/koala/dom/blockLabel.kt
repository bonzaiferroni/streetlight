package koala.dom

import koala.css.ModifierSet
import koala.css.setModifiers
import koala.html.TagAttribute
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
    setModifiers(modifiers)
    setAttribute(TagAttribute.blockLabel, label)
    block?.invoke(this)
}