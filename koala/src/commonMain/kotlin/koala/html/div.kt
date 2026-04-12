package koala.html

import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div as divTag

fun FlowContent.div(
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = { },
) {
    divTag {
        addModifiers(modifiers)
        block()
    }
}

fun FlowContent.div(
    id: Id,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = { },
) {
    divTag {
        setId(id)
        addModifiers(modifiers)
        block()
    }
}