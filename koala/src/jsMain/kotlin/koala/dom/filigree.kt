package koala.dom

import koala.modifier.*
import koala.html.configureFiligree
import kotlinx.html.DIV

fun AppendScope.filigree(
    mod: Modifier? = null,
    ruleMod: Modifier = MaxWidth(16),
    block: DIV.() -> Unit
) = row(modify(mod, JustifyContentCenter, AlignItemsCenter)) {
    configureFiligree(ruleMod, block)
}