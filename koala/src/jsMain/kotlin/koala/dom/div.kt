package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.html.Id
import koala.html.setId
import kotlinx.html.DIV
import kotlinx.html.js.div

fun AppendScope.div(
    modifiers: ModifierSet?,
    block: DIV.() -> Unit = { }
) = div {
    addModifiers(modifiers)
    block()
}

fun AppendScope.div(
    id: Id,
    modifiers: ModifierSet?,
    block: DIV.() -> Unit = { }
) = div(modifiers) {
    setId(id)
    block()
}