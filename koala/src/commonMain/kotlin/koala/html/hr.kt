package koala.html

import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.HR
import kotlinx.html.hr as hrElement

fun FlowContent.hr(
    mod: ModifierSet? = null,
    block: HR.() -> Unit = { }
) {
    hrElement {
        addModifiers(mod)
        block()
    }
}