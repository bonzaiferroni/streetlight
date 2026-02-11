package koala.html

import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.span
import kotlinx.html.SPAN

fun FlowOrPhrasingContent.textSpan(
    text: String,
    modifierSet: ModifierSet? = null,
    block: (SPAN.() -> Unit)? = null,
) {
    span {
        applyModifiers(modifierSet)
        +text
        block?.invoke(this)
    }
}