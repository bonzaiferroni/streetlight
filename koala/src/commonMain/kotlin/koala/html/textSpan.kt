package koala.html

import koala.css.ModifierSet
import koala.css.setModifiers
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.span
import kotlinx.html.SPAN

fun FlowOrPhrasingContent.textSpan(
    text: String,
    modifierSet: ModifierSet? = null,
    block: (SPAN.() -> Unit)? = null,
) {
    span {
        setModifiers(modifierSet)
        +text
        block?.invoke(this)
    }
}