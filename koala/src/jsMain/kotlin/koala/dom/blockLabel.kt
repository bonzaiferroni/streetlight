package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.html.Attribute
import koala.html.blockLabel
import koala.html.setAttribute
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement

fun DOM.blockLabel(
    label: String,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) = div {
    this.blockLabel = label
    addModifiers(modifiers)
    setAttribute(Attribute.BlockLabel, label)
    block?.invoke(this)
}

fun HTMLElement.setBlockLabel(label: String): HTMLElement {
    setAttribute(Attribute.BlockLabel.to(label))
    return this
}