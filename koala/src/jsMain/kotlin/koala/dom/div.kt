package koala.dom

import koala.modifier.ModifierSet
import koala.modifier.addModifiers
import koala.html.Id
import koala.html.setId
import kotlinx.html.DIV
import kotlinx.html.js.div

fun AppendScope.div(
    mod: ModifierSet?,
    block: DIV.() -> Unit = { }
) = div {
    addModifiers(mod)
    block()
}.asWeb()

fun AppendScope.div(
    id: Id,
    mod: ModifierSet?,
    block: DIV.() -> Unit = { }
) = div(mod) {
    setId(id)
    block()
}