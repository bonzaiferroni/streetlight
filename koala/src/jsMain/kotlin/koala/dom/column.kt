package koala.dom

import koala.css.Column
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.css.modify
import koala.html.Id
import koala.html.setId
import kotlinx.html.DIV
import kotlinx.html.js.div

fun TagScope.column(
    id: Id?,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = { },
) = column(modifiers) {
    setId(id)
    block()
}

fun TagScope.column(
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = { },
) = div {
    addModifiers(modify(Column, modifiers))
    block()
}