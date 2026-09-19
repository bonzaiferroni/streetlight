package koala.dom

import koala.modifier.ModifierSet
import koala.modifier.FlexRow
import koala.modifier.addModifiers
import koala.html.Id
import koala.modifier.Modifier
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun AppendScope.row(
    id: Id,
    mod: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = row(mod) {
    this.id = id.identifier
    content()
}

inline fun AppendScope.row(
    mod: Modifier? = null,
    crossinline content: DIV.() -> Unit,
) = div {
    addModifiers(FlexRow, mod)
    content()
}.asWeb()