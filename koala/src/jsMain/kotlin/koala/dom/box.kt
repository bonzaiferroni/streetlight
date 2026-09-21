package koala.dom

import koala.modifier.*
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun AppendScope.box(
    mod: Modifier? = null,
    crossinline content: DIV.() -> Unit = { },
) = div {
    addModifiers(Box, mod)
    content()
}.asWeb()

inline fun AppendScope.box(
    id: Id,
    mod: Modifier? = null,
    crossinline content: DIV.() -> Unit = { },
) = box(mod = mod) {
    this.id = id.identifier
    content()
}