package koala.dom

import koala.modifier.*
import koala.html.configureFiligree
import kotlinx.html.DIV

/** Frames what [block] builds, usually a heading, between two decorative rules. */
fun AppendScope.filigree(
    mod: Modifier? = null,
    ruleMod: Modifier = MaxWidth(16),
    block: DIV.() -> Unit
) = row(modify(mod, JustifyContentCenter, AlignItemsCenter)) {
    configureFiligree(ruleMod, block)
}