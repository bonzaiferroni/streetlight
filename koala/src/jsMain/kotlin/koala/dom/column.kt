package koala.dom

import koala.modifier.FlexColumn
import koala.modifier.addModifiers
import koala.modifier.modify
import koala.html.Id
import koala.html.setId
import koala.modifier.Modifier
import kotlinx.html.DIV
import kotlinx.html.js.div

/** A flex [column] with [id]. */
fun AppendScope.column(
    id: Id?,
    mod: Modifier? = null,
    content: DIV.() -> Unit = { },
) = column(mod) {
    setId(id)
    content()
}

/** A flex column. */
fun AppendScope.column(
    mod: Modifier? = null,
    content: DIV.() -> Unit = { },
) = div {
    addModifiers(modify(FlexColumn, mod))
    content()
}.asWeb()