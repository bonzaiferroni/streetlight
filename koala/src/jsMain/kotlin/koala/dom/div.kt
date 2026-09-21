package koala.dom

import koala.modifier.*
import koala.html.Id
import koala.html.setId
import kotlinx.html.DIV
import kotlinx.html.js.div

fun AppendScope.div(
    mod: Modifier?,
    block: DIV.() -> Unit = { }
) = div {
    addModifiers(mod)
    block()
}.asWeb()

fun AppendScope.div(
    id: Id,
    mod: Modifier?,
    block: DIV.() -> Unit = { }
) = div(mod) {
    setId(id)
    block()
}