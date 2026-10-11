package koala.dom.layout

import koala.modifier.*
import koala.page.Id
import koala.page.setId
import kotlinx.html.DIV
import kotlinx.html.js.div
import koala.dom.AppendScope
import koala.dom.asWeb

/** A `div` with [mod] applied. */
fun AppendScope.div(
    mod: Modifier?,
    block: DIV.() -> Unit = { }
) = div {
    addModifiers(mod)
    block()
}.asWeb()

/** A `div` with [id] and [mod] applied. */
fun AppendScope.div(
    id: Id,
    mod: Modifier?,
    block: DIV.() -> Unit = { }
) = div(mod) {
    setId(id)
    block()
}