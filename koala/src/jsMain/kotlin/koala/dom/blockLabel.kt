package koala.dom

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.html.HTMLElement

fun AppendScope.blockLabel(
    label: String,
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null
) = div {
    this.blockLabel = label
    addModifiers(mod)
    setAttribute(Attribute.BlockLabel, label)
    block?.invoke(this)
}.asWeb()

fun HTMLElement.setBlockLabel(label: String): HTMLElement {
    setAttribute(Attribute.BlockLabel.to(label))
    return this
}