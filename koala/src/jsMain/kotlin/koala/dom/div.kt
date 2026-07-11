package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.html.Id
import koala.html.setId
import kotlinx.html.DIV
import kotlinx.html.js.div

fun TagScope.div(
    mod: ModifierSet?,
    block: DIV.() -> Unit = { }
) = div {
    addModifiers(mod)
    block()
}

fun TagScope.div(
    id: Id,
    mod: ModifierSet?,
    block: DIV.() -> Unit = { }
) = div(mod) {
    setId(id)
    block()
}