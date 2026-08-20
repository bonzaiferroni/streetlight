package koala.dom

import koala.css.Column
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.css.modify
import koala.html.Id
import koala.html.setId
import kotlinx.html.DIV
import kotlinx.html.js.div

fun AppendScope.column(
    id: Id?,
    mod: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) = column(mod) {
    setId(id)
    content()
}

fun AppendScope.column(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) = div {
    addModifiers(modify(Column, mod))
    content()
}