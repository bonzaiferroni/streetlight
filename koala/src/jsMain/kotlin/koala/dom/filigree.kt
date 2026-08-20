package koala.dom

import koala.css.*
import koala.html.configureFiligree
import kotlinx.html.DIV

fun AppendScope.filigree(
    modifiers: ModifierSet? = null,
    ruleMaxWidth: Modifier = MaxWidth16,
    block: DIV.() -> Unit
) = row(modify(modifiers, JustifyContentCenter, AlignItemsCenter)) {
    configureFiligree(ruleMaxWidth, block)
}