package koala.dom.component

import koala.modifier.*
import kotlinx.html.DIV
import koala.dom.AppendScope
import koala.dom.layout.row
import koala.component.configureFiligree
import koala.component.heading

/** Frames what [block] builds, usually a heading, between two decorative rules. */
fun AppendScope.filigree(
    mod: Modifier? = null,
    ruleMod: Modifier = MaxWidth(16),
    block: DIV.() -> Unit
) = row(modify(mod, JustifyContentCenter, AlignItemsCenter)) {
    configureFiligree(ruleMod, block)
}