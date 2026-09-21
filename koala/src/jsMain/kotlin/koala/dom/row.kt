package koala.dom

import koala.modifier.*
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun AppendScope.row(
    id: Id,
    mod: Modifier? = null,
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